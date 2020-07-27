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

import net.consensys.besu.plugins.stream.core.Serializer;
import net.consensys.besu.plugins.types.DecodedLogWithMetadata;
import net.consensys.besu.plugins.types.Fixture;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class DecodedLogWithMetadataSerializerTest {

  @Test
  void serialize() {
    final JsonNode json =
        Serializer.serialize(
            new ObjectMapper(),
            new DecodedLogWithMetadata(
                Fixture.createLogWithMetadata(),
                "Transfer(0x0c2ca8977e5c582f938c30f7a5328ac1d101bd564,0x06e01587ad6b033d4b05156003c3412e8997bfe3,801976598137298011761447)"));

    assertThat(json.isObject()).isTrue();
    final ObjectNode jsonObject = (ObjectNode) json;

    final Set<String> expectedFields =
        Set.of(
            "removed",
            "logIndex",
            "transactionIndex",
            "transactionHash",
            "blockHash",
            "blockNumber",
            "address",
            "data",
            "topics",
            "decoded");
    assertThat(ImmutableList.copyOf(jsonObject.fieldNames()))
        .containsExactlyInAnyOrderElementsOf(expectedFields);
    assertThat(jsonObject.get("removed").asBoolean()).isFalse();
    assertThat(jsonObject.get("logIndex").asText()).isEqualTo("0x0");
    assertThat(jsonObject.get("transactionIndex").asText()).isEqualTo("0x0");
    assertThat(jsonObject.get("transactionHash").asText())
        .isEqualTo("0xabc0e2d93e34b8a6f093bb44e102684accd3e45a036df43e5d83b18aa74d40fa");
    assertThat(jsonObject.get("blockHash").asText())
        .isEqualTo("0x8478d0924e106a2e6ee040842316d015fa6a3b87b252060e975153ea238fb0f3");
    assertThat(jsonObject.get("blockNumber").asText()).isEqualTo("0x8ee6ac");
    assertThat(jsonObject.get("address").asText())
        .isEqualTo("0x1dea979ae76f26071870f824088da78979eb91c8");
    assertThat(jsonObject.get("data").asText())
        .isEqualTo("0x00000000000000000000000000000000000000000000a9d33d0e47bfd6c72b27");
    assertThat(jsonObject.get("topics").asText())
        .isEqualTo(
            "[0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef, 0x000000000000000000000000c2ca8977e5c582f938c30f7a5328ac1d101bd564, 0x00000000000000000000000006e01587ad6b033d4b05156003c3412e8997bfe3]");
    assertThat(jsonObject.get("decoded").asText())
        .isEqualTo(
            "Transfer(0x0c2ca8977e5c582f938c30f7a5328ac1d101bd564,0x06e01587ad6b033d4b05156003c3412e8997bfe3,801976598137298011761447)");
  }
}
