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

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.LogWithMetadata;

public class Fixture {
  public static final String HASH =
      "0x0e6319c0087f3bb7ca95388871ed5f210942a44bcfa0e67155f00f221e2b8a48";
  public static final String ADDRESS = "0xfe3b557e8fb62b89f4916b721be55ceb828dbd73";

  public static LogWithMetadata createLogWithMetadata() {
    return new LogWithMetadata() {
      @Override
      public Address getLogger() {
        return net.consensys.besu.plugins.types.Address.fromHexString(
            "0x1dea979ae76f26071870f824088da78979eb91c8");
      }

      @Override
      public Bytes getData() {
        return Bytes.fromHexStringLenient(
            "00000000000000000000000000000000000000000000a9d33d0e47bfd6c72b27");
      }

      @Override
      public int getLogIndex() {
        return 0;
      }

      @Override
      public long getBlockNumber() {
        return 9365164;
      }

      @Override
      public Hash getBlockHash() {
        return net.consensys.besu.plugins.types.Hash.fromHexString(
            "0x8478d0924e106a2e6ee040842316d015fa6a3b87b252060e975153ea238fb0f3");
      }

      @Override
      public Hash getTransactionHash() {
        return net.consensys.besu.plugins.types.Hash.fromHexString(
            "0xabc0e2d93e34b8a6f093bb44e102684accd3e45a036df43e5d83b18aa74d40fa");
      }

      @Override
      public int getTransactionIndex() {
        return 0;
      }

      @Override
      public List<Hash> getTopics() {
        return List.of(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x000000000000000000000000c2ca8977e5c582f938c30f7a5328ac1d101bd564",
                "0x00000000000000000000000006e01587ad6b033d4b05156003c3412e8997bfe3")
            .stream()
            .map(net.consensys.besu.plugins.types.Hash::fromHexString)
            .collect(toUnmodifiableList());
      }

      @Override
      public boolean isRemoved() {
        return false;
      }
    };
  }

  public static Hash createHash(final long value) {
    return net.consensys.besu.plugins.types.Hash.fromUnsignedLong(value);
  }

  public static Address createAddress(final long value) {
    return net.consensys.besu.plugins.types.Address.fromUnsignedLong(value);
  }
}
