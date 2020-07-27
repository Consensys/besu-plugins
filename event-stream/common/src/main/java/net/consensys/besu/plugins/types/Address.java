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

public class Address extends DelegatingBytes implements org.hyperledger.besu.plugin.data.Address {

  public static final int SIZE = 20;

  private Address(final byte[] bytes) {
    super(Bytes.wrap(bytes));
  }

  public static Address fromHexString(final String hex) {
    if (hex == null) return null;
    return wrap(Bytes.fromHexStringLenient(hex, SIZE));
  }

  public static Address fromUnsignedLong(final long value) {
    return new Address(ByteBuffer.allocate(20).position(12).putLong(value).array());
  }

  public static Address wrap(final Bytes value) {
    checkArgument(
        value.size() == SIZE,
        "An account address must be %s bytes long, got %s",
        SIZE,
        value.size());
    return new Address(value.toArray());
  }
}
