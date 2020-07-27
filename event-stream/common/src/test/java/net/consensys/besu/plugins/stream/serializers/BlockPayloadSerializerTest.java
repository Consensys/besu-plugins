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
import static net.consensys.besu.plugins.stream.util.BlockHeaderMockFixture.createBlockHeaderMock;
import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.besu.plugins.stream.core.Serializer;
import net.consensys.besu.plugins.stream.model.payload.BlockPayload;
import net.consensys.besu.plugins.types.Fixture;
import net.consensys.besu.plugins.types.QuantityFormatter;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlockPayloadSerializerTest {

  @Test
  void serializeBlockHeader() {
    final long now = now().toEpochMilli();
    final BlockHeader blockHeaderMock = createBlockHeaderMock(now);
    JsonNode json = Serializer.serialize(new ObjectMapper(), new BlockPayload(blockHeaderMock));
    assertThat(json).isNotNull();
    assertThat(json.isObject()).isTrue();
    checkIsValidBlockHeader(now, (ObjectNode) json);
  }

  @Test
  void serializeBlockHeaderWithDifficulty() {
    final long now = now().toEpochMilli();
    final BlockHeader blockHeaderMock = createBlockHeaderMock(now);
    JsonNode json =
        Serializer.serialize(
            new ObjectMapper(), new BlockPayload(blockHeaderMock, UInt256.fromHexString("0x11")));
    assertThat(json).isNotNull();
    assertThat(json.isObject()).isTrue();
    checkIsValidBlockHeader(now, (ObjectNode) json);
    assertThat(json.get("totalDifficulty").asText()).isEqualToIgnoringCase("0x11");
  }

  private void checkIsValidBlockHeader(final long now, final ObjectNode jsonObject) {

    final JsonNode blockNode = jsonObject.get("blockHeader");
    assertThat(blockNode.isObject()).isTrue();
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
    // Check all required fields are present
    assertThat(ImmutableList.copyOf(blockNode.fieldNames()))
        .containsExactlyInAnyOrderElementsOf(expectedFields);
    // Check each field
    assertThat(blockNode.get("hash").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("number").asText()).isEqualTo("0x1");
    assertThat(blockNode.get("nonce").asText()).isEqualTo("0x3");
    assertThat(blockNode.get("difficulty").asText()).isEqualTo("0x1");
    assertThat(blockNode.get("gasLimit").asText()).isEqualTo("0x5");
    assertThat(blockNode.get("gasUsed").asText()).isEqualTo("0x2");
    assertThat(blockNode.get("timestamp").asText()).isEqualTo(QuantityFormatter.format(now));
    assertThat(blockNode.get("coinbase").asText()).isEqualTo(Fixture.ADDRESS);
    assertThat(blockNode.get("extraData").asText()).isEqualTo(Bytes.EMPTY.toHexString());
    assertThat(blockNode.get("logsBloom").asText()).isEqualTo(Bytes.EMPTY.toHexString());
    assertThat(blockNode.get("mixHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("ommersHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("parentHash").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("receiptsRoot").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("transactionsRoot").asText()).isEqualTo(Fixture.HASH);
    assertThat(blockNode.get("stateRoot").asText()).isEqualTo(Fixture.HASH);
  }
}
