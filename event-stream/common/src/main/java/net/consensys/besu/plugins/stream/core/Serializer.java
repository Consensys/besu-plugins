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
package net.consensys.besu.plugins.stream.core;

import static java.util.stream.Collectors.toUnmodifiableList;

import net.consensys.besu.plugins.stream.model.payload.BlockPayload;
import net.consensys.besu.plugins.stream.model.payload.TransactionPayload;
import net.consensys.besu.plugins.types.DecodedLogWithMetadata;
import net.consensys.besu.plugins.types.QuantityFormatter;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.LogWithMetadata;
import org.hyperledger.besu.plugin.data.SyncStatus;
import org.hyperledger.besu.plugin.data.Transaction;

public interface Serializer {
  Logger LOG = LogManager.getLogger();

  static JsonNode serialize(final ObjectMapper mapper, final Optional<SyncStatus> maybeSyncStatus) {
    if (maybeSyncStatus.isPresent()) {
      final SyncStatus syncStatus = maybeSyncStatus.get();
      final ObjectNode eventNode = mapper.createObjectNode();
      eventNode
          .put("startingBlock", QuantityFormatter.format(syncStatus.getStartingBlock()))
          .put("currentBlock", QuantityFormatter.format(syncStatus.getCurrentBlock()))
          .put("highestBlock", QuantityFormatter.format(syncStatus.getHighestBlock()));
      return eventNode;
    } else {
      return BooleanNode.FALSE;
    }
  }

  static JsonNode serialize(final ObjectMapper mapper, final BlockHeader blockHeader) {
    final ObjectNode eventNode = mapper.createObjectNode();
    eventNode
        .put("hash", blockHeader.getBlockHash().toHexString())
        .put("number", QuantityFormatter.format(blockHeader.getNumber()))
        .put("nonce", QuantityFormatter.format(blockHeader.getNonce()))
        .put("difficulty", QuantityFormatter.format(blockHeader.getDifficulty()))
        .put("gasLimit", QuantityFormatter.format(blockHeader.getGasLimit()))
        .put("gasUsed", QuantityFormatter.format(blockHeader.getGasUsed()))
        .put("timestamp", QuantityFormatter.format(blockHeader.getTimestamp()))
        .put("coinbase", blockHeader.getCoinbase().toHexString())
        .put("extraData", blockHeader.getExtraData().toHexString())
        .put("logsBloom", blockHeader.getLogsBloom().toHexString())
        .put("mixHash", blockHeader.getMixHash().toHexString())
        .put("ommersHash", blockHeader.getOmmersHash().toHexString())
        .put("parentHash", blockHeader.getParentHash().toHexString())
        .put("receiptsRoot", blockHeader.getReceiptsRoot().toHexString())
        .put("transactionsRoot", blockHeader.getTransactionsRoot().toHexString())
        .put("stateRoot", blockHeader.getStateRoot().toHexString());

    return eventNode;
  }

  static JsonNode serialize(final ObjectMapper mapper, final Transaction transaction) {
    final ObjectNode eventNode = mapper.createObjectNode();
    eventNode
        .put("nonce", QuantityFormatter.format(transaction.getNonce()))
        .put("gas", QuantityFormatter.format(transaction.getGasLimit()))
        .put("value", QuantityFormatter.format(transaction.getValue()))
        .put("v", QuantityFormatter.format(transaction.getV()))
        .put("r", QuantityFormatter.format(transaction.getR()))
        .put("s", QuantityFormatter.format(transaction.getS()))
        .put("from", transaction.getSender().toHexString())
        .put("input", transaction.getPayload().toHexString())
        .put("hash", transaction.getHash().toHexString());
    transaction
        .getGasPrice()
        .ifPresent(
            gasPrice -> {
              eventNode.put("gasPrice", QuantityFormatter.format(transaction.getGasPrice().get()));
            });
    transaction
        .getMaxFeePerGas()
        .ifPresent(
            gasPrice -> {
              eventNode.put(
                  "maxFeePerGas", QuantityFormatter.format(transaction.getMaxFeePerGas().get()));
              eventNode.put(
                  "maxPriorityFeePerGas",
                  QuantityFormatter.format(transaction.getMaxPriorityFeePerGas().get()));
            });
    transaction.getTo().map(Address::toHexString).ifPresent(to -> eventNode.put("to", to));
    transaction
        .getChainId()
        .map(QuantityFormatter::format)
        .ifPresent(chainId -> eventNode.put("chainId", chainId));

    return eventNode;
  }

  static JsonNode serialize(final ObjectMapper mapper, final BlockPayload blockPayload) {
    ObjectNode objectNode =
        mapper
            .createObjectNode()
            .set("blockHeader", serialize(mapper, blockPayload.getBlockHeader()));
    blockPayload
        .getTotalDifficulty()
        .ifPresent(
            totalDifficulty ->
                objectNode.put(
                    "totalDifficulty", QuantityFormatter.format(totalDifficulty.toBigInteger())));
    return objectNode;
  }

  static JsonNode serialize(
      final ObjectMapper mapper, final TransactionPayload transactionPayload) {
    final ObjectNode eventNode = mapper.createObjectNode();
    transactionPayload
        .getBlockHeader()
        .ifPresent(blockHeader -> eventNode.set("blockHeader", serialize(mapper, blockHeader)));
    eventNode.set("transaction", serialize(mapper, transactionPayload.getTransaction()));
    transactionPayload
        .getMaybeRevertReason()
        .ifPresent(revertReason -> eventNode.put("revertReason", revertReason));
    return eventNode;
  }

  static JsonNode serialize(final ObjectMapper mapper, final LogWithMetadata logWithMetadata) {
    final ObjectNode result =
        mapper
            .createObjectNode()
            .put("blockNumber", QuantityFormatter.format(logWithMetadata.getBlockNumber()))
            .put("blockHash", logWithMetadata.getBlockHash().toHexString())
            .put("transactionHash", logWithMetadata.getTransactionHash().toHexString())
            .put(
                "transactionIndex", QuantityFormatter.format(logWithMetadata.getTransactionIndex()))
            .put("address", logWithMetadata.getLogger().toHexString())
            .put("data", logWithMetadata.getData().toHexString())
            .put(
                "topics",
                logWithMetadata.getTopics().stream()
                    .map(Bytes::toHexString)
                    .collect(toUnmodifiableList())
                    .toString())
            .put("removed", logWithMetadata.isRemoved())
            .put("logIndex", QuantityFormatter.format(logWithMetadata.getLogIndex()));

    if (logWithMetadata instanceof DecodedLogWithMetadata) {
      result.put("decoded", ((DecodedLogWithMetadata) logWithMetadata).getDecoded());
    }
    return result;
  }
}
