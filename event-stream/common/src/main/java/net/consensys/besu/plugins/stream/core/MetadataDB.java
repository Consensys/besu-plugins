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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import com.google.common.primitives.Longs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.services.BesuConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.StorageService;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorageTransaction;
import org.hyperledger.besu.plugin.services.storage.SegmentIdentifier;

public class MetadataDB {
  private static final Logger LOG = LogManager.getLogger();

  private final KeyValueStorage storage;

  static MetadataDB create(final BesuContext context) {
    return new MetadataDB(
        context
            .getService(StorageService.class)
            .orElseThrow(
                () -> new IllegalStateException("Missing mandatory StorageService service."))
            .getByName("rocksdb")
            .orElseThrow(
                () ->
                    new IllegalStateException("Missing mandatory rocksdb KeyValueStorageFactory."))
            .create(
                createSegmentIdentifier("BLOCKCHAIN", new byte[] {0x1}),
                context
                    .getService(BesuConfiguration.class)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Missing mandatory BesuConfiguration service.")),
                context
                    .getService(MetricsSystem.class)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Missing mandatory MetricsSystem service."))));
  }

  private MetadataDB(final KeyValueStorage storage) {
    this.storage = storage;
  }

  Optional<Long> getLatestBlockNumber() {
    return storage.get(Keys.LATEST_BLOCK_NUMBER_KEY).map(Longs::fromByteArray);
  }

  void setLatestBlockNumber(final long latestBlockNumber) {
    put(Keys.LATEST_BLOCK_NUMBER_KEY, Longs.toByteArray(latestBlockNumber));
  }

  Optional<byte[]> getLatestBlockHash() {
    return storage.get(Keys.LATEST_BLOCK_HASH_KEY);
  }

  void setLatestBlockHash(final Hash latestBlockHash) {
    put(Keys.LATEST_BLOCK_HASH_KEY, latestBlockHash.toArray());
  }

  void setLatestBlockHash(final byte[] latestBlockHash) {
    put(Keys.LATEST_BLOCK_HASH_KEY, latestBlockHash);
  }

  private void put(final byte[] key, final byte[] value) {
    final KeyValueStorageTransaction tx = storage.startTransaction();
    try {
      tx.put(key, value);
      tx.commit();
    } catch (Exception e) {
      LOG.error("Cannot store value in config store.", e);
      tx.rollback();
    }
  }

  void putAllSingleTransaction(final Map<byte[], byte[]> entries) {
    final KeyValueStorageTransaction tx = storage.startTransaction();
    try {
      entries.forEach(tx::put);
      tx.commit();
    } catch (Exception e) {
      LOG.error("Cannot store value in config store.", e);
      tx.rollback();
    }
  }

  private static SegmentIdentifier createSegmentIdentifier(final String name, final byte[] id) {
    return new SegmentIdentifier() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public byte[] getId() {
        return id;
      }
    };
  }

  static class Keys {
    static final byte[] LATEST_BLOCK_NUMBER_KEY =
        "LATEST_BLOCK_NUMBER".getBytes(StandardCharsets.UTF_8);
    static final byte[] LATEST_BLOCK_HASH_KEY =
        "LATEST_BLOCK_HASH".getBytes(StandardCharsets.UTF_8);
  }
}
