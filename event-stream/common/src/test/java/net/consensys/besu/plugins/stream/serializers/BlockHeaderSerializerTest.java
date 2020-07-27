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

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.core.Serializer;
import net.consensys.besu.plugins.types.Fixture;
import net.consensys.besu.plugins.types.QuantityFormatter;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.Quantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlockHeaderSerializerTest {
  @Mock private Address senderMock;
  @Mock private Hash hashMock;
  @Mock private Quantity quantityMock;
  @Mock private BlockHeader blockHeader;

  @Test
  void serializeBlockHeader() {
    when(senderMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(quantityMock.getValue()).thenReturn(5L);
    long now = now().toEpochMilli();
    when(blockHeader.getParentHash()).thenReturn(hashMock);
    when(blockHeader.getOmmersHash()).thenReturn(hashMock);
    when(blockHeader.getCoinbase()).thenReturn(senderMock);
    when(blockHeader.getStateRoot()).thenReturn(hashMock);
    when(blockHeader.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeader.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeader.getLogsBloom()).thenReturn(Bytes.EMPTY);
    when(blockHeader.getDifficulty()).thenReturn(quantityMock);
    when(blockHeader.getNumber()).thenReturn(1L);
    when(blockHeader.getGasLimit()).thenReturn(5L);
    when(blockHeader.getGasUsed()).thenReturn(2L);
    when(blockHeader.getTimestamp()).thenReturn(now);
    when(blockHeader.getExtraData()).thenReturn(Bytes.EMPTY);
    when(blockHeader.getMixHash()).thenReturn(hashMock);
    when(blockHeader.getNonce()).thenReturn(3L);
    when(blockHeader.getBlockHash()).thenReturn(hashMock);
    JsonNode json = Serializer.serialize(new ObjectMapper(), blockHeader);
    assertThat(json).isNotNull();
    assertThat(json.isObject()).isTrue();
    final ObjectNode jsonObject = (ObjectNode) json;

    final List<String> expectedFields =
        Arrays.asList(
            "hash",
            "number",
            "nonce",
            "difficulty",
            "gasLimit",
            "gasUsed",
            "timestamp",
            "coinbase",
            "extraData",
            "logsBloom",
            "mixHash",
            "ommersHash",
            "parentHash",
            "receiptsRoot",
            "transactionsRoot",
            "stateRoot");
    assertThat(ImmutableList.copyOf(jsonObject.fieldNames()))
        .containsExactlyInAnyOrderElementsOf(expectedFields);
    assertThat(jsonObject.get("hash").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("number").asText()).isEqualTo("0x1");
    assertThat(jsonObject.get("nonce").asText()).isEqualTo("0x3");
    assertThat(jsonObject.get("difficulty").asText()).isEqualTo("0x5");
    assertThat(jsonObject.get("gasLimit").asText()).isEqualTo("0x5");
    assertThat(jsonObject.get("gasUsed").asText()).isEqualTo("0x2");
    assertThat(jsonObject.get("timestamp").asText()).isEqualTo(QuantityFormatter.format(now));
    assertThat(jsonObject.get("coinbase").asText()).isEqualTo(Fixture.ADDRESS);
    assertThat(jsonObject.get("extraData").asText()).isEqualTo(Bytes.EMPTY.toHexString());
    assertThat(jsonObject.get("logsBloom").asText()).isEqualTo(Bytes.EMPTY.toHexString());
    assertThat(jsonObject.get("mixHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("ommersHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("parentHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("receiptsRoot").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("transactionsRoot").asText()).isEqualTo(Fixture.HASH);
    assertThat(jsonObject.get("stateRoot").asText()).isEqualTo(Fixture.HASH);
  }
}
