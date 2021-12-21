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

import java.math.BigInteger;

import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.besu.plugin.data.Quantity;

public class BigIntegerQuantity implements Quantity {
  private final BigInteger value;

  public BigIntegerQuantity(final BigInteger value) {
    this.value = value;
  }

  @Override
  public Number getValue() {
    return value;
  }

  @Override
  public BigInteger getAsBigInteger() {
    return value;
  }

  @Override
  public String toHexString() {
    return "0x" + value.toString(16);
  }

  @Override
  public String toShortHexString() {
    return Hex.toHexString(value.toByteArray());
  }
}
