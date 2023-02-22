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

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.ByteBuffer;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.DelegatingBytes;

/** A {@link Bytes} that also represents an Ethereum account address. */
public class Address extends DelegatingBytes implements org.hyperledger.besu.plugin.data.Address {

  /** The constant SIZE. */
  public static final int SIZE = 20;

  /**
   * Instantiates a new Address.
   *
   * @param bytes the bytes
   */
  private Address(final byte[] bytes) {
    super(Bytes.wrap(bytes));
  }

  /**
   * Parse a hexadecimal string representing an account address.
   *
   * @param hex A hexadecimal string (with or without the leading '0x') representing a valid account
   *     address.
   * @return The parsed address: {@code null} if the provided string is {@code null}.
   * @throws IllegalArgumentException if the string is either not hexadecimal, or not the valid
   *     representation of an address.
   */
  public static Address fromHexString(final String hex) {
    if (hex == null) return null;
    return wrap(Bytes.fromHexStringLenient(hex, SIZE));
  }

  /**
   * Parse an unsigned long representing an account address.
   *
   * @param value An unsigned long representing a valid account address.
   * @return The parsed address
   * @throws IllegalArgumentException if the long is not the valid representation of an address.
   */
  public static Address fromUnsignedLong(final long value) {
    return new Address(ByteBuffer.allocate(20).position(12).putLong(value).array());
  }

  /**
   * Wrap address.
   *
   * @param value the value
   * @return the address
   */
  public static Address wrap(final Bytes value) {
    checkArgument(
        value.size() == SIZE,
        "An account address must be %s bytes long, got %s",
        SIZE,
        value.size());
    return new Address(value.toArray());
  }
}
