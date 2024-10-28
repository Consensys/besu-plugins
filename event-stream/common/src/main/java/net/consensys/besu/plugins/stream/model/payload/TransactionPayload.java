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
package net.consensys.besu.plugins.stream.model.payload;

import java.util.Optional;

import org.hyperledger.besu.datatypes.Transaction;
import org.hyperledger.besu.plugin.data.BlockHeader;

/** This class defines the payload that is sent during an Event linked to a transaction */
public class TransactionPayload {

  private final Transaction transaction;
  private final Optional<BlockHeader> blockHeader;
  private final Optional<String> revertReason;

  /**
   * Create a new transaction payload
   *
   * @param transaction the transaction
   * @param blockHeader the block header
   * @param revertReason the revert reason
   */
  public TransactionPayload(
      final Transaction transaction, final BlockHeader blockHeader, final String revertReason) {
    this.transaction = transaction;
    this.blockHeader = Optional.ofNullable(blockHeader);
    this.revertReason = Optional.ofNullable(revertReason);
  }

  /**
   * Create a new transaction payload with empty block header and revert reason
   *
   * @param transaction the transaction
   */
  public TransactionPayload(final Transaction transaction) {
    this.transaction = transaction;
    blockHeader = Optional.empty();
    revertReason = Optional.empty();
  }

  /**
   * Returns info related to the transaction
   *
   * @return a {@link Transaction}
   */
  public Transaction getTransaction() {
    return transaction;
  }

  /**
   * Return the block header of the transaction
   *
   * @return an {@link Optional} of {@link BlockHeader} of the block which contains the transaction
   *     if available
   */
  public Optional<BlockHeader> getBlockHeader() {
    return blockHeader;
  }

  /**
   * Return the revert reason of the transaction
   *
   * @return the revert reason in case of failure
   */
  public Optional<String> getMaybeRevertReason() {
    return revertReason;
  }
}
