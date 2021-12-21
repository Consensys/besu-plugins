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

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.api.config.EventStreamConfiguration;
import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.core.config.EventSchema;
import net.consensys.besu.plugins.stream.core.config.EventSchemas;
import net.consensys.besu.plugins.stream.model.DefaultEvent;
import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.stream.model.payload.BlockPayload;
import net.consensys.besu.plugins.stream.model.payload.TransactionPayload;
import net.consensys.besu.plugins.stream.util.BlockHeaderMockFixture;
import net.consensys.besu.plugins.stream.util.TransactionMockFixture;
import net.consensys.besu.plugins.types.DecodedLogWithMetadata;
import net.consensys.besu.plugins.types.Fixture;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.plugin.data.AddedBlockContext;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.BlockBody;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.LogWithMetadata;
import org.hyperledger.besu.plugin.data.PropagatedBlockContext;
import org.hyperledger.besu.plugin.data.Quantity;
import org.hyperledger.besu.plugin.data.SyncStatus;
import org.hyperledger.besu.plugin.data.Transaction;
import org.hyperledger.besu.plugin.data.TransactionReceipt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.abi.TypeReference;

@ExtendWith(MockitoExtension.class)
class BesuEventListenerTest {

  @Mock private Publisher publisher;
  @Mock private MetadataDB metadataDB;
  @Captor private ArgumentCaptor<Event> eventCaptor;
  private final TopicResolver fixedTopicResolver = new TopicResolver.Fixed(() -> "test-topic");
  private static final String BYTES_ERROR_MESSAGE =
      "0x08c379a00000000000000000000000000000000000000"
          + "000000000000000000000000020000000000000000000000000000000000000000000000000000000000000002a"
          + "6e6f7420706f737369626c6520746f20636f6d6d6974206461746120616674657220646561646c696e650000000"
          + "0000000000000000000000000000000000000";
  private static final String BYTES_MISSING_ERROR_MESSAGE =
      "0x0000000000000000000000000000000000000"
          + "000000000000000000000000020000000000000000000000000000000000000000000000000000000000000002a"
          + "6e6f7420706f737369626c6520746f20636f6d6d6974206461746120616674657220646561646c696e650000000"
          + "0000000000000000000000000000000000000";

  @SuppressWarnings("unchecked")
  @Test
  void blockPropagated() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final Hash hashMock = mock(Hash.class);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(hashMock.toArray())
        .thenReturn(new BigInteger(Fixture.HASH.substring(2), 16).toByteArray());
    final Address addressMock = mock(Address.class);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    final Quantity quantityMock = mock(Quantity.class);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);
    final BlockHeader blockHeaderMock = mock(BlockHeader.class);
    final PropagatedBlockContext propagatedBlockContextMock = mock(PropagatedBlockContext.class);
    when(propagatedBlockContextMock.getBlockHeader()).thenReturn(blockHeaderMock);
    when(propagatedBlockContextMock.getTotalDifficulty()).thenReturn(UInt256.ONE);
    when(blockHeaderMock.getParentHash()).thenReturn(hashMock);
    when(blockHeaderMock.getCoinbase()).thenReturn(addressMock);
    when(blockHeaderMock.getDifficulty()).thenReturn(quantityMock);
    when(blockHeaderMock.getNumber()).thenReturn(1L);
    when(blockHeaderMock.getGasLimit()).thenReturn(1L);
    when(blockHeaderMock.getTimestamp()).thenReturn(System.currentTimeMillis());
    when(blockHeaderMock.getOmmersHash()).thenReturn(hashMock);
    when(blockHeaderMock.getStateRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getLogsBloom()).thenReturn(Bytes.fromHexStringLenient("0", 256));
    when(blockHeaderMock.getGasUsed()).thenReturn(1L);
    when(blockHeaderMock.getExtraData()).thenReturn(Bytes.fromHexString("4321"));
    when(blockHeaderMock.getMixHash()).thenReturn(hashMock);
    when(blockHeaderMock.getNonce()).thenReturn(1L);
    when(blockHeaderMock.getBlockHash()).thenReturn(hashMock);
    besuEventListener.onBlockPropagated(propagatedBlockContextMock);
    verify(publisher)
        .publish(eq(DomainObjectType.BLOCK), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(BlockPayload.class);
    assertThat(((DefaultEvent<BlockPayload>) eventCaptor.getValue()).getEvent().getBlockHeader())
        .isEqualTo(blockHeaderMock);
    verify(hashMock, times(7)).toHexString();
    verify(addressMock, times(1)).toHexString();
    verify(quantityMock, times(1)).getValue();
  }

  @SuppressWarnings("unchecked")
  @Test
  void transactionAdded() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final Transaction transactionMock = mock(Transaction.class);
    final Address addressMock = mock(Address.class);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    final Quantity quantityMock = mock(Quantity.class);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);
    final Hash hashMock = mock(Hash.class);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(transactionMock.getNonce()).thenReturn(1L);
    when(transactionMock.getGasPrice()).thenAnswer(invocation -> Optional.of(quantityMock));
    when(transactionMock.getGasLimit()).thenReturn(1L);
    when(transactionMock.getValue()).thenReturn(quantityMock);
    when(transactionMock.getV()).thenReturn(BigInteger.ONE);
    when(transactionMock.getR()).thenReturn(BigInteger.ONE);
    when(transactionMock.getS()).thenReturn(BigInteger.ONE);
    when(transactionMock.getSender()).thenReturn(addressMock);
    when(transactionMock.getTo()).thenReturn(Optional.empty());
    when(transactionMock.getHash()).thenReturn(hashMock);
    when(transactionMock.getPayload()).thenReturn(Bytes.fromHexString("1234"));
    besuEventListener.onTransactionAdded(transactionMock);
    verify(publisher)
        .publish(eq(DomainObjectType.TRANSACTION), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(TransactionPayload.class);
    assertThat(
            ((DefaultEvent<TransactionPayload>) eventCaptor.getValue()).getEvent().getTransaction())
        .isEqualTo(transactionMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  void transactionDropped() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final Transaction transactionMock = mock(Transaction.class);
    final Address addressMock = mock(Address.class);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    final Quantity quantityMock = mock(Quantity.class);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);
    final Hash hashMock = mock(Hash.class);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(transactionMock.getNonce()).thenReturn(1L);
    when(transactionMock.getGasPrice()).thenAnswer(invocation -> Optional.of(quantityMock));
    when(transactionMock.getGasLimit()).thenReturn(1L);
    when(transactionMock.getValue()).thenReturn(quantityMock);
    when(transactionMock.getV()).thenReturn(BigInteger.ONE);
    when(transactionMock.getR()).thenReturn(BigInteger.ONE);
    when(transactionMock.getS()).thenReturn(BigInteger.ONE);
    when(transactionMock.getSender()).thenReturn(addressMock);
    when(transactionMock.getTo()).thenReturn(Optional.empty());
    when(transactionMock.getHash()).thenReturn(hashMock);
    when(transactionMock.getPayload()).thenReturn(Bytes.fromHexString("1234"));
    besuEventListener.onTransactionDropped(transactionMock);
    verify(publisher)
        .publish(eq(DomainObjectType.TRANSACTION), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(TransactionPayload.class);
    assertThat(
            ((DefaultEvent<TransactionPayload>) eventCaptor.getValue()).getEvent().getTransaction())
        .isEqualTo(transactionMock);
  }

  @Test
  void syncStatusChanged_syncStarted() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final Optional<SyncStatus> syncStatus = Optional.of(mock(SyncStatus.class));
    besuEventListener.onSyncStatusChanged(syncStatus);
    verify(publisher)
        .publish(eq(DomainObjectType.NODE), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent()).isEqualTo(syncStatus);
  }

  @Test
  void syncStatusChanged_syncStopped() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final Optional<SyncStatus> syncStatus = Optional.empty();
    besuEventListener.onSyncStatusChanged(syncStatus);
    verify(publisher)
        .publish(eq(DomainObjectType.NODE), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent()).isEqualTo(syncStatus);
  }

  @Test
  @SuppressWarnings("unchecked")
  void log() throws ClassNotFoundException {
    final LogWithMetadata logWithMetadata = Fixture.createLogWithMetadata();
    final EventStreamConfiguration configuration = mock(EventStreamConfiguration.class);
    when(configuration.getEventSchemas())
        .thenReturn(
            new EventSchemas(
                Set.of(
                    new EventSchema(
                        "randomRealLog",
                        net.consensys.besu.plugins.types.Address.fromHexString(
                            logWithMetadata.getLogger().toHexString()),
                        "Transfer",
                        List.of(
                            TypeReference.makeTypeReference("address", true, true),
                            TypeReference.makeTypeReference("address", true, true),
                            TypeReference.makeTypeReference("uint256", false, true))))));
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher,
            fixedTopicResolver,
            Optional.of(metadataDB),
            MoreExecutors.directExecutor(),
            configuration);
    besuEventListener.onLogEmitted(logWithMetadata);
    final DecodedLogWithMetadata expected =
        new DecodedLogWithMetadata(
            logWithMetadata,
            "Transfer(0xc2ca8977e5c582f938c30f7a5328ac1d101bd564,0x06e01587ad6b033d4b05156003c3412e8997bfe3,801976598137298011761447)");
    verify(publisher)
        .publish(eq(DomainObjectType.LOG), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isEqualToComparingFieldByFieldRecursively(expected);
  }

  @SuppressWarnings("unchecked")
  @Test
  void blockAddedWithoutRevertReason() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final AddedBlockContext addedBlockContextMock = mock(AddedBlockContext.class);
    final BlockHeader blockHeaderMock = mock(BlockHeader.class);
    when(addedBlockContextMock.getBlockHeader()).thenReturn(blockHeaderMock);
    final Hash hashMock = mock(Hash.class);
    final Address addressMock = mock(Address.class);
    final Quantity quantityMock = mock(Quantity.class);
    when(blockHeaderMock.getBlockHash()).thenReturn(hashMock);
    when(blockHeaderMock.getNumber()).thenReturn(1L);
    when(blockHeaderMock.getNonce()).thenReturn(2L);
    when(blockHeaderMock.getDifficulty()).thenReturn(quantityMock);
    when(blockHeaderMock.getGasLimit()).thenReturn(5L);
    when(blockHeaderMock.getGasUsed()).thenReturn(3L);
    when(blockHeaderMock.getTimestamp()).thenReturn(now().toEpochMilli());
    when(blockHeaderMock.getCoinbase()).thenReturn(addressMock);
    when(blockHeaderMock.getExtraData()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getLogsBloom()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getMixHash()).thenReturn(hashMock);
    when(blockHeaderMock.getOmmersHash()).thenReturn(hashMock);
    when(blockHeaderMock.getParentHash()).thenReturn(hashMock);
    when(blockHeaderMock.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getStateRoot()).thenReturn(hashMock);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);

    besuEventListener.onBlockAdded(addedBlockContextMock);
    verify(publisher)
        .publish(eq(DomainObjectType.BLOCK), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(BlockPayload.class);
    assertThat(((DefaultEvent<BlockPayload>) eventCaptor.getValue()).getEvent().getBlockHeader())
        .isEqualTo(blockHeaderMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  void blockAddedWithRevertReason() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final long now = now().toEpochMilli();
    final AddedBlockContext addedBlockContextMock = mock(AddedBlockContext.class);
    final BlockHeader blockHeaderMock = BlockHeaderMockFixture.createBlockHeaderMock(now);
    doReturn(blockHeaderMock).when(addedBlockContextMock).getBlockHeader();

    final TransactionReceipt transactionReceiptMock = mock(TransactionReceipt.class);
    doReturn(Collections.singletonList(transactionReceiptMock))
        .when(addedBlockContextMock)
        .getTransactionReceipts();

    final BlockBody blockBodyMock = mock(BlockBody.class);
    doReturn(blockBodyMock).when(addedBlockContextMock).getBlockBody();
    final Transaction transactionMock = TransactionMockFixture.createTransactionMock();
    doReturn(Collections.singletonList(transactionMock)).when(blockBodyMock).getTransactions();

    Bytes bytes = Bytes.fromHexString(BYTES_ERROR_MESSAGE);
    when(transactionReceiptMock.getRevertReason()).thenReturn(Optional.of(bytes));

    besuEventListener.onBlockAdded(addedBlockContextMock);
    verify(publisher)
        .publish(eq(DomainObjectType.BLOCK), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(BlockPayload.class);
    assertThat(((DefaultEvent<BlockPayload>) eventCaptor.getValue()).getEvent().getBlockHeader())
        .isEqualTo(blockHeaderMock);
    verify(publisher)
        .publish(eq(DomainObjectType.TRANSACTION), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(TransactionPayload.class);
    assertThat(
            ((DefaultEvent<TransactionPayload>) eventCaptor.getValue()).getEvent().getTransaction())
        .isEqualTo(transactionMock);
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getType())
        .isEqualTo(Event.Type.TRANSACTION_REVERTED);
  }

  @Test
  @SuppressWarnings("unchecked")
  void blockAddedWithEmptyRevertReason() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final AddedBlockContext addedBlockContextMock = mock(AddedBlockContext.class);
    final BlockHeader blockHeaderMock = mock(BlockHeader.class);
    final BlockBody blockBodyMock = mock(BlockBody.class);
    final Transaction transactionMock = mock(Transaction.class);
    when(addedBlockContextMock.getBlockHeader()).thenReturn(blockHeaderMock);
    when(addedBlockContextMock.getBlockBody()).thenReturn(blockBodyMock);
    doReturn(Collections.singletonList(transactionMock)).when(blockBodyMock).getTransactions();
    final Hash hashMock = mock(Hash.class);
    final Address addressMock = mock(Address.class);
    final Quantity quantityMock = mock(Quantity.class);
    when(blockHeaderMock.getBlockHash()).thenReturn(hashMock);
    when(blockHeaderMock.getNumber()).thenReturn(1L);
    when(blockHeaderMock.getNonce()).thenReturn(2L);
    when(blockHeaderMock.getDifficulty()).thenReturn(quantityMock);
    when(blockHeaderMock.getGasLimit()).thenReturn(5L);
    when(blockHeaderMock.getGasUsed()).thenReturn(3L);
    when(blockHeaderMock.getTimestamp()).thenReturn(now().toEpochMilli());
    when(blockHeaderMock.getCoinbase()).thenReturn(addressMock);
    when(blockHeaderMock.getExtraData()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getLogsBloom()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getMixHash()).thenReturn(hashMock);
    when(blockHeaderMock.getOmmersHash()).thenReturn(hashMock);
    when(blockHeaderMock.getParentHash()).thenReturn(hashMock);
    when(blockHeaderMock.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getStateRoot()).thenReturn(hashMock);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);

    final TransactionReceipt transactionReceiptMock = mock(TransactionReceipt.class);
    doReturn(Collections.singletonList(transactionReceiptMock))
        .when(addedBlockContextMock)
        .getTransactionReceipts();
    Bytes bytes = Bytes.fromHexString(BYTES_MISSING_ERROR_MESSAGE);
    when(transactionReceiptMock.getRevertReason()).thenReturn(Optional.of(bytes));

    besuEventListener.onBlockAdded(addedBlockContextMock);
    verify(publisher)
        .publish(eq(DomainObjectType.BLOCK), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(BlockPayload.class);
    assertThat(((DefaultEvent<BlockPayload>) eventCaptor.getValue()).getEvent().getBlockHeader())
        .isEqualTo(blockHeaderMock);
    verifyNoMoreInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  @Test
  void blockReorged() {
    final BesuEventListener besuEventListener =
        new BesuEventListener(
            publisher, fixedTopicResolver, Optional.of(metadataDB), MoreExecutors.directExecutor());
    final AddedBlockContext addedBlockContextMock = mock(AddedBlockContext.class);
    final BlockHeader blockHeaderMock = mock(BlockHeader.class);
    when(addedBlockContextMock.getBlockHeader()).thenReturn(blockHeaderMock);
    final Hash hashMock = mock(Hash.class);
    final Address addressMock = mock(Address.class);
    final Quantity quantityMock = mock(Quantity.class);
    when(blockHeaderMock.getBlockHash()).thenReturn(hashMock);
    when(blockHeaderMock.getNumber()).thenReturn(1L);
    when(blockHeaderMock.getNonce()).thenReturn(2L);
    when(blockHeaderMock.getDifficulty()).thenReturn(quantityMock);
    when(blockHeaderMock.getGasLimit()).thenReturn(5L);
    when(blockHeaderMock.getGasUsed()).thenReturn(3L);
    when(blockHeaderMock.getTimestamp()).thenReturn(now().toEpochMilli());
    when(blockHeaderMock.getCoinbase()).thenReturn(addressMock);
    when(blockHeaderMock.getExtraData()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getLogsBloom()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getMixHash()).thenReturn(hashMock);
    when(blockHeaderMock.getOmmersHash()).thenReturn(hashMock);
    when(blockHeaderMock.getParentHash()).thenReturn(hashMock);
    when(blockHeaderMock.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getStateRoot()).thenReturn(hashMock);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);

    besuEventListener.onBlockReorg(addedBlockContextMock);
    verify(publisher)
        .publish(eq(DomainObjectType.BLOCK), eq(fixedTopicResolver), eventCaptor.capture());
    assertThat(((DefaultEvent<?>) eventCaptor.getValue()).getEvent())
        .isInstanceOf(BlockPayload.class);
    assertThat(((DefaultEvent<BlockPayload>) eventCaptor.getValue()).getEvent().getBlockHeader())
        .isEqualTo(blockHeaderMock);
  }
}
