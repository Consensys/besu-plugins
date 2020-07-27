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
import net.consensys.besu.plugins.stream.model.payload.TransactionPayload;
import net.consensys.besu.plugins.stream.util.TransactionMockFixture;
import net.consensys.besu.plugins.types.Fixture;
import net.consensys.besu.plugins.types.QuantityFormatter;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionPayloadSerializerTest {

  @Test
  void serialiseTransaction() {

    final Transaction transactionMock = TransactionMockFixture.createTransactionMock();

    final TransactionPayload transactionPayload = new TransactionPayload(transactionMock);

    final JsonNode json = Serializer.serialize(new ObjectMapper(), transactionPayload);
    assertThat(json.isObject()).isTrue();
    checkIsValidTransaction((ObjectNode) json);
  }

  @Test
  void serialiseTransactionWithBlockHeaderAndRevertReason() {

    final long now = now().toEpochMilli();

    final BlockHeader blockHeaderMock = createBlockHeaderMock(now);
    final Transaction transactionMock = TransactionMockFixture.createTransactionMock();
    final String revertReason = "Nonce already used";

    final TransactionPayload transactionPayload =
        new TransactionPayload(transactionMock, blockHeaderMock, revertReason);

    final JsonNode json = Serializer.serialize(new ObjectMapper(), transactionPayload);
    assertThat(json.isObject()).isTrue();
    checkIsValidBlockHeader(now, (ObjectNode) json);
    checkIsValidTransaction((ObjectNode) json);
    assertThat(json.get("revertReason").asText()).isEqualTo(revertReason);
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

  private void checkIsValidTransaction(final ObjectNode jsonObject) {

    final JsonNode transactionNode = jsonObject.get("transaction");
    assertThat(transactionNode.isObject()).isTrue();
    final List<String> expectedFields =
        Arrays.asList("from", "gas", "gasPrice", "nonce", "value", "v", "r", "s", "input", "hash");
    assertThat(ImmutableList.copyOf(transactionNode.fieldNames()))
        .containsExactlyInAnyOrderElementsOf(expectedFields);
    assertThat(transactionNode.get("from").asText()).isEqualTo(Fixture.ADDRESS);
    assertThat(transactionNode.get("nonce").asText()).isEqualTo("0x1");
    assertThat(transactionNode.get("gasPrice").asText()).isEqualTo("0x7d0");
    assertThat(transactionNode.get("gas").asText()).isEqualTo("0x3");
    assertThat(transactionNode.get("value").asText()).isEqualTo("0x0");
    assertThat(transactionNode.get("v").asText()).isEqualTo("0x5");
    assertThat(transactionNode.get("r").asText()).isEqualTo("0x6");
    assertThat(transactionNode.get("s").asText()).isEqualTo("0x7");
    assertThat(transactionNode.get("input").asText()).isEqualTo("0x1234");
    assertThat(transactionNode.get("hash").asText()).isEqualTo(Fixture.HASH);
  }
}
