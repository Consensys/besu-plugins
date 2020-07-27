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

import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Transaction;

/** This class defines the payload that is sent during an Event linked to a transaction */
public class TransactionPayload {

  private final Transaction transaction;
  private final Optional<BlockHeader> blockHeader;
  private final Optional<String> revertReason;

  public TransactionPayload(Transaction transaction, BlockHeader blockHeader, String revertReason) {
    this.transaction = transaction;
    this.blockHeader = Optional.ofNullable(blockHeader);
    this.revertReason = Optional.ofNullable(revertReason);
  }

  public TransactionPayload(Transaction transaction) {
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
