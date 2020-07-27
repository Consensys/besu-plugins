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
package net.consensys.besu.plugins.stream.core.config;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.besu.plugins.types.Address;
import net.consensys.besu.plugins.types.Hash;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

public class EventSchemasTest {
  public EventSchemasTest() throws IOException {}

  final EventSchemas actual =
      EventSchemas.from(
          new File(this.getClass().getResource("/example-event-schema-config.yaml").getFile()));

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void loadsConfigYaml() throws ClassNotFoundException {
    final List<TypeReference<Type>> balanceIndexedParameters =
        List.of(
            TypeReference.makeTypeReference("bytes32", true, true),
            TypeReference.makeTypeReference("address", true, true),
            TypeReference.makeTypeReference("address", true, true));
    final List<TypeReference<Type>> balanceUpdateNonIndexedParameters =
        List.of(
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true));
    final List<TypeReference<Type>> balanceParameters =
        Stream.concat(balanceIndexedParameters.stream(), balanceUpdateNonIndexedParameters.stream())
            .collect(toUnmodifiableList());
    final List<TypeReference<Type>> purchasePowerParameters = balanceParameters.subList(0, 12);
    final List<TypeReference<Type>> securityParameters = new ArrayList<>(balanceIndexedParameters);
    securityParameters.addAll(
        List.<TypeReference<Type>>of(
            TypeReference.makeTypeReference("bytes32", true, true),
            TypeReference.makeTypeReference("address", true, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true)));

    final List<TypeReference<Type>> cashIssuanceBalanceUpdateParams =
        new ArrayList<>(balanceIndexedParameters);
    cashIssuanceBalanceUpdateParams.addAll(
        List.<TypeReference<Type>>of(
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true)));

    final List<TypeReference<Type>> cashBrokerFeeUpdateParams =
        new ArrayList<>(balanceIndexedParameters);
    cashBrokerFeeUpdateParams.addAll(
        List.<TypeReference<Type>>of(
            TypeReference.makeTypeReference("address", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("int256", false, true),
            TypeReference.makeTypeReference("uint256", false, true)));

    final List<TypeReference<Type>> securityCreationParams =
        new ArrayList<>(balanceIndexedParameters);
    securityCreationParams.addAll(
        List.<TypeReference<Type>>of(
            TypeReference.makeTypeReference("string", false, true),
            TypeReference.makeTypeReference("uint", false, true)));

    final List<TypeReference<Type>> anonymousTestParameters =
        List.of(
            TypeReference.makeTypeReference("address", true, true),
            TypeReference.makeTypeReference("address", true, true),
            TypeReference.makeTypeReference("uint256", false, true));

    assertThat(actual)
        .isEqualTo(
            new EventSchemas(
                Set.of(
                    new EventSchema(
                        "cashBalanceUpdate",
                        Address.fromHexString("0xcb4f3cA3777fE16FBF4595Ba48d0eBFAEfEaBEBc"),
                        "CashBalanceUpdate",
                        balanceParameters),
                    new EventSchema(
                        "cashIssuanceBalanceUpdate",
                        Address.fromHexString("0xcb4f3cA3777fE16FBF4595Ba48d0eBFAEfEaBEBc"),
                        "CashIssuanceUpdate",
                        cashIssuanceBalanceUpdateParams),
                    new EventSchema(
                        "dvpCashBrokerFeeUpdate",
                        Address.fromHexString("0x1622c3352f54f66E2b86583958D30DB50695ec4c"),
                        "CashBrokerFeeUpdate",
                        cashBrokerFeeUpdateParams),
                    new EventSchema(
                        "dvpSecurityBalanceUpdate",
                        Address.fromHexString("0x1622c3352f54f66E2b86583958D30DB50695ec4c"),
                        "SecurityBalanceUpdate",
                        securityParameters),
                    new EventSchema(
                        "dvpCashBalanceUpdate",
                        Address.fromHexString("0x1622c3352f54f66E2b86583958D30DB50695ec4c"),
                        "CashBalanceUpdate",
                        balanceParameters),
                    new EventSchema(
                        "dvpPurchasePowerBalanceUpdate",
                        Address.fromHexString("0x1622c3352f54f66E2b86583958D30DB50695ec4c"),
                        "PurchasePowerBalanceUpdate",
                        purchasePowerParameters),
                    new EventSchema(
                        "purchasePowerCashBalanceUpdate",
                        Address.fromHexString("0x3bCA05EeDe696ae836C45E1Ed928cDB06D33fb9E"),
                        "CashBalanceUpdate",
                        balanceParameters),
                    new EventSchema(
                        "purchasePowerPurchasePowerBalanceUpdate",
                        Address.fromHexString("0x3bCA05EeDe696ae836C45E1Ed928cDB06D33fb9E"),
                        "PurchasePowerBalanceUpdate",
                        purchasePowerParameters),
                    new EventSchema(
                        "reservationsSecurityBalanceUpdate",
                        Address.fromHexString("0x80742a9f6FFdaDE2Cc578e5d23A1de84661E4822"),
                        "SecurityBalanceUpdate",
                        securityParameters),
                    new EventSchema(
                        "reservationsCashBalanceUpdate",
                        Address.fromHexString("0x80742a9f6FFdaDE2Cc578e5d23A1de84661E4822"),
                        "CashBalanceUpdate",
                        balanceParameters),
                    new EventSchema(
                        "reservationsPurchasePowerBalanceUpdate",
                        Address.fromHexString("0x80742a9f6FFdaDE2Cc578e5d23A1de84661E4822"),
                        "PurchasePowerBalanceUpdate",
                        purchasePowerParameters),
                    new EventSchema(
                        "securitiesSecurityBalanceUpdate",
                        Address.fromHexString("0x3efc4Ed3D2BB2D1eE577fA546d8453a6d67d7467"),
                        "SecurityBalanceUpdate",
                        securityParameters),
                    new EventSchema(
                        "issuanceIssuanceBalanceUpdate",
                        Address.fromHexString("0x464c01295B7a736CE04974301683000f333cF142"),
                        "IssuanceBalanceUpdate",
                        securityParameters.subList(0, 9)),
                    new EventSchema(
                        "issuanceSecurityCreation",
                        Address.fromHexString("0x464c01295B7a736CE04974301683000f333cF142"),
                        "SecurityCreation",
                        securityCreationParams),
                    new EventSchema(
                        "anonymousTestEvent",
                        Address.fromHexString("0x464c01295B7a736CE04974301683000f333cF142"),
                        null,
                        anonymousTestParameters))));
  }

  @Test
  public void topicsHashCorrectly() {
    assertThat(actual.getSchemas().stream().map(EventSchema::getTopic))
        .contains(
            Hash.fromHexString(
                "0xf3a901a87e49175e43f16ddf5625084caa0f8315e66b126ce3a109782436d23e"));
  }
}
