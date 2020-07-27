/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.consensys.besu.plugins.stream.core;

import static java.util.concurrent.Executors.newCachedThreadPool;

import net.consensys.besu.plugins.stream.api.config.EventStreamConfiguration;
import net.consensys.besu.plugins.stream.api.errors.SerializationException;
import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.EventSerializer;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DefaultEvent;
import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.stream.model.payload.BlockPayload;
import net.consensys.besu.plugins.stream.model.payload.TransactionPayload;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.data.AddedBlockContext;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.LogWithMetadata;
import org.hyperledger.besu.plugin.data.PropagatedBlockContext;
import org.hyperledger.besu.plugin.data.SyncStatus;
import org.hyperledger.besu.plugin.data.Transaction;
import org.hyperledger.besu.plugin.data.TransactionReceipt;
import org.hyperledger.besu.plugin.services.BesuEvents;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Type;

/**
 * This class listens events from the Ethereum client and delegates event handling to specific
 * listeners.
 *
 * @since 0.1
 */
public class BesuEventListener
    implements BesuEvents.BlockPropagatedListener,
        BesuEvents.BlockAddedListener,
        BesuEvents.BlockReorgListener,
        BesuEvents.TransactionAddedListener,
        BesuEvents.TransactionDroppedListener,
        BesuEvents.SyncStatusListener,
        BesuEvents.LogListener {

  private static final Logger LOGGER = LogManager.getLogger(BesuEventListener.class);
  private final Executor executor;
  private final Publisher publisher;
  private final TopicResolver topicResolver;
  private final Optional<net.consensys.besu.plugins.stream.core.MetadataDB> configStore;
  private final EventStreamConfiguration configuration;

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final List<TypeReference<Type>> revertReasonType =
      Collections.singletonList(TypeReference.create((Class<Type>) AbiTypes.getType("string")));

  private static final Bytes ERROR_METHOD_ID = Bytes.fromHexString("0x08c379a0");

  static BesuEventListener create(
      final BesuContext context,
      final Publisher publisher,
      final TopicResolver topicResolver,
      final EventStreamConfiguration configuration) {
    return new BesuEventListener(
        publisher,
        topicResolver,
        configuration.isMetadataDBEnabled()
            ? Optional.of(net.consensys.besu.plugins.stream.core.MetadataDB.create(context))
            : Optional.empty(),
        configuration);
  }

  @VisibleForTesting
  BesuEventListener(
      final Publisher publisher,
      final TopicResolver topicResolver,
      final Optional<net.consensys.besu.plugins.stream.core.MetadataDB> configStore,
      final Executor executor) {
    this.publisher = publisher;
    this.topicResolver = topicResolver;
    this.configStore = configStore;
    this.executor = executor;
    this.configuration = null;
  }

  BesuEventListener(
      final Publisher publisher,
      final TopicResolver topicResolver,
      final Optional<net.consensys.besu.plugins.stream.core.MetadataDB> configStore,
      final Executor executor,
      final EventStreamConfiguration configuration) {
    this.publisher = publisher;
    this.topicResolver = topicResolver;
    this.configStore = configStore;
    this.executor = executor;
    this.configuration = configuration;
  }

  private BesuEventListener(
      final Publisher publisher,
      final TopicResolver topicResolver,
      final Optional<net.consensys.besu.plugins.stream.core.MetadataDB> configStore,
      final EventStreamConfiguration configuration) {
    this(publisher, topicResolver, configStore, newCachedThreadPool(), configuration);
  }

  @Override
  public void onBlockPropagated(final PropagatedBlockContext propagatedBlockContext) {
    if (shouldBePublished(
        propagatedBlockContext.getBlockHeader().getNumber(),
        propagatedBlockContext.getBlockHeader().getBlockHash())) {
      applyEvent(
          DomainObjectType.BLOCK,
          Event.Type.BLOCK_PROPAGATED,
          new BlockPayload(
              propagatedBlockContext.getBlockHeader(), propagatedBlockContext.getTotalDifficulty()),
          net.consensys.besu.plugins.stream.core.Serializer::serialize);
      configStore.ifPresent(
          store ->
              store.putAllSingleTransaction(
                  ImmutableMap.<byte[], byte[]>builder()
                      .put(
                          net.consensys.besu.plugins.stream.core.MetadataDB.Keys
                              .LATEST_BLOCK_NUMBER_KEY,
                          Longs.toByteArray(propagatedBlockContext.getBlockHeader().getNumber()))
                      .put(
                          net.consensys.besu.plugins.stream.core.MetadataDB.Keys
                              .LATEST_BLOCK_HASH_KEY,
                          propagatedBlockContext.getBlockHeader().getBlockHash().toArray())
                      .build()));
    }
  }

  private boolean shouldBePublished(final long blockNumber, final Hash blockHash) {
    return configStore
        .flatMap(net.consensys.besu.plugins.stream.core.MetadataDB::getLatestBlockNumber)
        .map(
            latest ->
                (blockNumber > latest)
                    || (configStore.get().getLatestBlockHash().isPresent()
                        && (blockNumber == latest)
                        && !Arrays.equals(
                            configStore.get().getLatestBlockHash().get(), blockHash.toArray())))
        .orElse(true);
  }

  @Override
  public void onBlockAdded(final AddedBlockContext addedBlockContext) {
    applyEvent(
        DomainObjectType.BLOCK,
        Event.Type.BLOCK_ADDED,
        new BlockPayload(addedBlockContext.getBlockHeader()),
        net.consensys.besu.plugins.stream.core.Serializer::serialize);

    onRevertedTransaction(addedBlockContext);
  }

  @Override
  public void onBlockReorg(final AddedBlockContext addedBlockContext) {
    applyEvent(
        DomainObjectType.BLOCK,
        Event.Type.BLOCK_REORG,
        new BlockPayload(addedBlockContext.getBlockHeader()),
        net.consensys.besu.plugins.stream.core.Serializer::serialize);
  }

  @SuppressWarnings("rawtypes")
  private void onRevertedTransaction(AddedBlockContext addedBlockContext) {
    final List<? extends TransactionReceipt> transactionReceipts =
        addedBlockContext.getTransactionReceipts();
    final BlockHeader blockHeader = addedBlockContext.getBlockHeader();

    for (int i = 0; i < transactionReceipts.size(); i++) {

      final Optional<Bytes> maybeRevertReason = transactionReceipts.get(i).getRevertReason();
      final Transaction transaction = addedBlockContext.getBlockBody().getTransactions().get(i);

      maybeRevertReason
          .filter(
              revertReasonBytes ->
                  revertReasonBytes.commonPrefixLength(ERROR_METHOD_ID) == ERROR_METHOD_ID.size())
          .map(
              revertReasonBytes ->
                  FunctionReturnDecoder.decode(
                      revertReasonBytes.slice(ERROR_METHOD_ID.size()).toHexString(),
                      revertReasonType))
          .map(
              decodedRevertReason ->
                  new TransactionPayload(
                      transaction, blockHeader, decodedRevertReason.get(0).getValue().toString()))
          .ifPresent(
              transactionPayload ->
                  applyEvent(
                      DomainObjectType.TRANSACTION,
                      Event.Type.TRANSACTION_REVERTED,
                      transactionPayload,
                      net.consensys.besu.plugins.stream.core.Serializer::serialize));
    }
  }

  @Override
  public void onTransactionAdded(final Transaction transaction) {
    applyEvent(
        DomainObjectType.TRANSACTION,
        Event.Type.TRANSACTION_ADDED,
        new TransactionPayload(transaction),
        net.consensys.besu.plugins.stream.core.Serializer::serialize);
  }

  @Override
  public void onTransactionDropped(final Transaction transaction) {
    applyEvent(
        DomainObjectType.TRANSACTION,
        Event.Type.TRANSACTION_DROPPED,
        new TransactionPayload(transaction),
        net.consensys.besu.plugins.stream.core.Serializer::serialize);
  }

  @Override
  public void onSyncStatusChanged(final Optional<SyncStatus> syncStatus) {
    applyEvent(
        DomainObjectType.NODE,
        Event.Type.SYNC_STATUS_CHANGED,
        syncStatus,
        net.consensys.besu.plugins.stream.core.Serializer::serialize);
  }

  @Override
  public void onLogEmitted(final LogWithMetadata logWithMetadata) {
    applyEvent(
        DomainObjectType.LOG,
        Event.Type.LOG_EMITTED,
        net.consensys.besu.plugins.stream.core.LogDecoder.decode(
            configuration.getEventSchemas(), logWithMetadata),
        net.consensys.besu.plugins.stream.core.Serializer::serialize);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> void applyEvent(
      final DomainObjectType domainObjectType,
      final String type,
      final T payload,
      final EventSerializer<T> serializer) {
    try {
      LOGGER.debug("Publishing message of type: {}", type);
      final DefaultEvent<T> event = DefaultEvent.create(type, payload, serializer);
      executor.execute(() -> publisher.publish(domainObjectType, topicResolver, event));
    } catch (final SerializationException e) {
      LOGGER.warn("Cannot publish event.", e);
    }
  }
}
