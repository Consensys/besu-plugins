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

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.hyperledger.besu.plugin.data.SyncStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncStatusSerializerTest {

  @Mock private SyncStatus syncStatusMock;

  @Test
  void serialise_presentSyncStatus() {
    when(syncStatusMock.getStartingBlock()).thenReturn(0L);
    when(syncStatusMock.getCurrentBlock()).thenReturn(2L);
    when(syncStatusMock.getHighestBlock()).thenReturn(32L);
    JsonNode json = Serializer.serialize(new ObjectMapper(), Optional.of(syncStatusMock));

    assertThat(json.isObject()).isTrue();
    final ObjectNode jsonObject = (ObjectNode) json;
    assertThat(ImmutableList.copyOf(jsonObject.fieldNames()))
        .containsExactlyInAnyOrder("startingBlock", "currentBlock", "highestBlock");
    assertThat(jsonObject.get("startingBlock").asText()).isEqualTo("0x0");
    assertThat(jsonObject.get("currentBlock").asText()).isEqualTo("0x2");
    assertThat(jsonObject.get("highestBlock").asText()).isEqualTo("0x20");
  }

  @Test
  void serialise_emptySyncStatus() {
    JsonNode json = Serializer.serialize(new ObjectMapper(), Optional.empty());

    assertThat(json.isBoolean()).isTrue();
    assertThat(json.asBoolean()).isEqualTo(false);
  }
}
