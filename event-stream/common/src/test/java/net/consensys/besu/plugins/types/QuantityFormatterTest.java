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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

class QuantityFormatterTest {

  @Test
  void assertThatZeroValueLongReturns0x0() {
    assertThat(QuantityFormatter.format(0)).isEqualTo("0x0");
  }

  @Test
  void assertThatLongQuantityDoesNotHaveLeadingZeros() {
    assertThat(QuantityFormatter.format(4885)).doesNotStartWith("0x00");
  }

  @Test
  void assertThatZeroValueBigIntegerReturns0x0() {
    assertThat(QuantityFormatter.format(BigInteger.ZERO)).isEqualTo("0x0");
  }

  @Test
  void assertThatBigIntegerQuantityDoesNotHaveLeadingZeros() {
    assertThat(QuantityFormatter.format(new BigInteger(256, ThreadLocalRandom.current())))
        .doesNotStartWith("0x00");
  }

  @Test
  void assertThatZeroValueQuantityReturns0x0() {
    assertThat(QuantityFormatter.format(new BigIntegerQuantity(BigInteger.ZERO))).isEqualTo("0x0");
  }

  @Test
  void assertThatQuantityDoesNotHaveLeadingZeros() {
    final byte[] sourceBytes = new byte[8];
    sourceBytes[7] = 0x01;
    final BigInteger bigInteger = new BigInteger(sourceBytes);
    final BigIntegerQuantity bigIntQuantity = new BigIntegerQuantity(bigInteger);
    assertThat(QuantityFormatter.format(bigIntQuantity)).isEqualTo("0x1");
  }
}
