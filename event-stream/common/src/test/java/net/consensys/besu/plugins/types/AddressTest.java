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

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class AddressTest {

  private final String ADDRESS = "0x0000000000000000000000000000000000101010";;

  @Test
  public void accountAddressToString() {
    final Address addr = Address.wrap(Bytes.fromHexString(ADDRESS));
    assertThat(addr.toString()).isEqualTo(ADDRESS);
  }

  @Test
  public void accountAddressEquals() {
    final Address addr = Address.wrap(Bytes.fromHexString(ADDRESS));
    final Address addr2 = Address.wrap(Bytes.fromHexString(ADDRESS));

    assertThat(addr2).isEqualByComparingTo(addr);
  }

  @Test
  public void accountAddressHashCode() {
    final Address addr = Address.wrap(Bytes.fromHexString(ADDRESS));
    final Address addr2 = Address.wrap(Bytes.fromHexString(ADDRESS));

    assertThat(addr2.hashCode()).isEqualTo(addr.hashCode());
  }

  @Test
  public void tooShortAccountAddressGetsPadded() {
    final Address addrShort = Address.fromHexString("0x00101010");
    final Address addr = Address.fromHexString(ADDRESS);
    assertThat(addr.hashCode()).isEqualTo(addrShort.hashCode());
  }
}
