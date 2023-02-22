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

import java.nio.ByteBuffer;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.DelegatingBytes32;

/** A 32-bytes hash value as used in Ethereum blocks, usually the result of a hashing algorithm. */
public class Hash extends DelegatingBytes32 implements org.hyperledger.besu.plugin.data.Hash {

  /**
   * Convert value to a hash.
   *
   * @param data the value
   * @return the hash
   */
  private Hash(final byte[] data) {
    super(Bytes.wrap(data));
  }

  /**
   * Wrap bytes to hash.
   *
   * @param data the bytes
   * @return the hash
   */
  public static Hash wrap(final byte[] data) {
    return new Hash(data);
  }

  /**
   * Parse a hexadecimal string representing a hash value.
   *
   * @param s hexadecimal string (with or without the leading '0x') representing a valid hash value.
   * @return The parsed hash.
   * @throws NullPointerException if the provided string is {@code null}.
   * @throws IllegalArgumentException if the string is either not hexadecimal, or not the valid
   *     representation of a hash (not 32 bytes).
   */
  public static Hash fromHexString(final String s) {
    return Hash.wrap(Bytes32.fromHexString(s).toArray());
  }

  /**
   * Parse an unsigned long representing a hash value.
   *
   * @param value unsigned long representing a valid hash value.
   * @return The parsed hash.
   */
  public static Hash fromUnsignedLong(final long value) {
    return Hash.wrap(ByteBuffer.allocate(32).position(24).putLong(value).array());
  }
}
