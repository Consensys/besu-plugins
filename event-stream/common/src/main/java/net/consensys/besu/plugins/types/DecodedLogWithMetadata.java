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
package net.consensys.besu.plugins.types;

import java.util.List;

import com.google.common.base.MoreObjects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.LogWithMetadata;

public class DecodedLogWithMetadata implements LogWithMetadata {

  private final int logIndex;
  private final long blockNumber;
  private final Hash blockHash;
  private final Hash transactionHash;
  private final int transactionIndex;
  private final boolean removed;
  private final Address address;
  private final Bytes data;
  private final List<? extends Bytes32> topics;
  private final String decoded;

  DecodedLogWithMetadata(
      final int logIndex,
      final long blockNumber,
      final Hash blockHash,
      final Hash transactionHash,
      final int transactionIndex,
      final Address address,
      final Bytes data,
      final List<? extends Bytes32> topics,
      final boolean removed,
      final String decoded) {
    this.logIndex = logIndex;
    this.blockNumber = blockNumber;
    this.blockHash = blockHash;
    this.transactionHash = transactionHash;
    this.transactionIndex = transactionIndex;
    this.address = address;
    this.data = data;
    this.topics = topics;
    this.removed = removed;
    this.decoded = decoded;
  }

  public DecodedLogWithMetadata(final LogWithMetadata baseLogWithMetadata, final String decoded) {
    this(
        baseLogWithMetadata.getLogIndex(),
        baseLogWithMetadata.getBlockNumber(),
        baseLogWithMetadata.getBlockHash(),
        baseLogWithMetadata.getTransactionHash(),
        baseLogWithMetadata.getTransactionIndex(),
        baseLogWithMetadata.getLogger(),
        baseLogWithMetadata.getData(),
        baseLogWithMetadata.getTopics(),
        baseLogWithMetadata.isRemoved(),
        decoded);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("logIndex", logIndex)
        .add("blockNumber", blockNumber)
        .add("blockHash", blockHash)
        .add("transactionHash", transactionHash)
        .add("transactionIndex", transactionIndex)
        .add("address", address)
        .add("data", data)
        .add("topics", topics)
        .add("removed", removed)
        .add("decoded", decoded)
        .toString();
  }

  @Override
  public Address getLogger() {
    return address;
  }

  @Override
  public List<? extends Bytes32> getTopics() {
    return topics;
  }

  @Override
  public Bytes getData() {
    return data;
  }

  @Override
  public int getLogIndex() {
    return logIndex;
  }

  @Override
  public long getBlockNumber() {
    return blockNumber;
  }

  @Override
  public Hash getBlockHash() {
    return blockHash;
  }

  @Override
  public Hash getTransactionHash() {
    return transactionHash;
  }

  @Override
  public int getTransactionIndex() {
    return transactionIndex;
  }

  @Override
  public boolean isRemoved() {
    return removed;
  }

  public String getDecoded() {
    return decoded;
  }
}
