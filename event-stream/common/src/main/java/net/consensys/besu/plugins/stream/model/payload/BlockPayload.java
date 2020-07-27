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

import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.plugin.data.BlockHeader;

/** This class defines the payload that is sent during an Event linked to a block */
public class BlockPayload {

  private final BlockHeader blockHeader;
  private final Optional<UInt256> totalDifficulty;

  public BlockPayload(BlockHeader blockHeader, UInt256 totalDifficulty) {
    this.blockHeader = blockHeader;
    this.totalDifficulty = Optional.ofNullable(totalDifficulty);
  }

  public BlockPayload(BlockHeader blockHeader) {
    this.blockHeader = blockHeader;
    totalDifficulty = Optional.empty();
  }

  /**
   * A {@link BlockHeader} object.
   *
   * @return A {@link BlockHeader}
   */
  public BlockHeader getBlockHeader() {
    return blockHeader;
  }

  /**
   * A scalar value corresponding to the total difficulty.
   *
   * @return An {{@link Optional}} of UInt256 value corresponding to the total difficulty.
   */
  public Optional<UInt256> getTotalDifficulty() {
    return totalDifficulty;
  }
}
