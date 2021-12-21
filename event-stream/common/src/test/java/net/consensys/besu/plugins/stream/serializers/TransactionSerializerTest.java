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
package net.consensys.besu.plugins.stream.serializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.core.Serializer;
import net.consensys.besu.plugins.types.BigIntegerQuantity;
import net.consensys.besu.plugins.types.Fixture;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionSerializerTest {
  @Mock private Address senderMock;
  @Mock private Hash hashMock;
  @Mock private Transaction transactionMock;

  @Test
  void serialise() {
    when(senderMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(transactionMock.getNonce()).thenReturn(1L);
    when(transactionMock.getGasPrice())
        .thenAnswer(invocation -> Optional.of(new BigIntegerQuantity(BigInteger.valueOf(0x7d0))));
    when(transactionMock.getGasLimit()).thenReturn(3L);
    when(transactionMock.getTo()).thenReturn(Optional.empty());
    when(transactionMock.getValue()).thenReturn(new BigIntegerQuantity(BigInteger.valueOf(0)));
    when(transactionMock.getV()).thenReturn(BigInteger.valueOf(5L));
    when(transactionMock.getR()).thenReturn(BigInteger.valueOf(6L));
    when(transactionMock.getS()).thenReturn(BigInteger.valueOf(7L));
    when(transactionMock.getSender()).thenReturn(senderMock);
    when(transactionMock.getChainId()).thenReturn(Optional.empty());
    when(transactionMock.getHash()).thenReturn(hashMock);
    when(transactionMock.getPayload()).thenReturn(Bytes.fromHexString("1234"));
    final JsonNode json = Serializer.serialize(new ObjectMapper(), transactionMock);
    assertThat(json.isObject()).isTrue();
    final ObjectNode jsonObject = (ObjectNode) json;

    final List<String> expectedFields =
        Arrays.asList("from", "gas", "gasPrice", "nonce", "value", "v", "r", "s", "input", "hash");
    assertThat(ImmutableList.copyOf(jsonObject.fieldNames()))
        .containsExactlyInAnyOrderElementsOf(expectedFields);
    assertThat(jsonObject.get("from").asText()).isEqualTo(Fixture.ADDRESS);
    assertThat(jsonObject.get("nonce").asText()).isEqualTo("0x1");
    assertThat(jsonObject.get("gasPrice").asText()).isEqualTo("0x7d0");
    assertThat(jsonObject.get("gas").asText()).isEqualTo("0x3");
    assertThat(jsonObject.get("value").asText()).isEqualTo("0x0");
    assertThat(jsonObject.get("v").asText()).isEqualTo("0x5");
    assertThat(jsonObject.get("r").asText()).isEqualTo("0x6");
    assertThat(jsonObject.get("s").asText()).isEqualTo("0x7");
    assertThat(jsonObject.get("input").asText()).isEqualTo("0x1234");
    assertThat(jsonObject.get("hash").asText()).isEqualTo(Fixture.HASH);
  }
}
