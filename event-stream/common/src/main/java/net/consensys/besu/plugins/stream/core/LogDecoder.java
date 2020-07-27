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

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toUnmodifiableList;

import net.consensys.besu.plugins.stream.core.config.EventSchema;
import net.consensys.besu.plugins.stream.core.config.EventSchemas;
import net.consensys.besu.plugins.types.DecodedLogWithMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.LogWithMetadata;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

public class LogDecoder {
  private LogDecoder() {}

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static LogWithMetadata decode(
      final EventSchemas eventSchemas, final LogWithMetadata logWithMetadata) {
    final Optional<List<? extends Bytes32>> eventTopics =
        (logWithMetadata.getTopics() == null) || logWithMetadata.getTopics().isEmpty()
            ? Optional.empty()
            : Optional.of(logWithMetadata.getTopics());
    final Address loggingContract = logWithMetadata.getLogger();
    Set<EventSchema> matchingSchemas =
        eventTopics.isEmpty()
            ? Collections.emptySet()
            : eventSchemas.getSchemas().stream()
                .filter(
                    eventSchema ->
                        eventSchema.getTopic().equals(eventTopics.get().get(0))
                            && eventSchema.getContractAddress().equals(loggingContract))
                .collect(Collectors.toSet());

    if (matchingSchemas.isEmpty() && eventTopics.isPresent()) {
      // anonymous event with indexed event parameters
      matchingSchemas =
          eventSchemas.getSchemas().stream()
              .filter(
                  eventSchema ->
                      (eventSchema.getContractAddress().equals(loggingContract)
                          && (eventSchema.getEventName() == null)
                          && eventSchema.getParameterTypes().stream()
                                  .filter(TypeReference::isIndexed)
                                  .collect(toUnmodifiableList())
                                  .size()
                              == eventTopics.get().size()))
              .collect(Collectors.toSet());
    }

    checkState(
        matchingSchemas.size() <= 1,
        "More than one matching event schema identified for event with address %s, topic %s",
        loggingContract,
        eventTopics.map(topics -> topics.get(0)).orElse(null));

    // if there's a matching schema for the log, decode the arguments
    return matchingSchemas.stream()
        .findAny()
        .<LogWithMetadata>map(
            matchingSchema -> {
              final List<TypeReference<Type>> parameterTypes = matchingSchema.getParameterTypes();

              // First fill a list with the arguments decoded from the data field
              final List<Type> decodedArguments =
                  FunctionReturnDecoder.decode(
                      logWithMetadata.getData().toHexString(),
                      parameterTypes.stream()
                          .filter(parameterType -> !parameterType.isIndexed())
                          .collect(toUnmodifiableList()));

              // Then decode the indexed arguments (which are topics), and insert them into the list
              // from above at the appropriate indices
              final int numberOfParamTypes = parameterTypes.size();
              final List<Integer> indexedParameterIndices =
                  IntStream.range(0, numberOfParamTypes)
                      .filter(i -> parameterTypes.get(i).isIndexed())
                      .boxed()
                      .collect(toUnmodifiableList());

              final boolean isAnonymous = matchingSchema.getEventName() == null;
              final List<Bytes32> indexedTopics =
                  logWithMetadata.getTopics().stream()
                      // Skip the first topic since it's the hash of the entire log signature
                      .skip(isAnonymous ? 0 : 1)
                      .collect(toUnmodifiableList());

              final List<TypeReference> indexedTopicTypes =
                  parameterTypes.stream()
                      .filter(TypeReference::isIndexed)
                      .collect(toUnmodifiableList());

              IntStream.range(0, indexedTopics.size())
                  .forEach(
                      i ->
                          decodedArguments.add(
                              indexedParameterIndices.get(i),
                              FunctionReturnDecoder.decodeIndexedValue(
                                  indexedTopics.get(i).toHexString(), indexedTopicTypes.get(i))));

              checkState(
                  decodedArguments.size() == numberOfParamTypes, "Mismatched number of args");

              return new DecodedLogWithMetadata(
                  logWithMetadata,
                  String.format(
                      "%s%s",
                      isAnonymous ? "" : matchingSchema.getEventName(),
                      decodedArguments.stream()
                          .map(Type::getValue)
                          .collect(toUnmodifiableList())
                          .toString()
                          .replace(" ", "")
                          .replace('[', '(')
                          .replace(']', ')')));
            })
        // if there's no matching schema return the same thing we were passed
        .orElse(logWithMetadata);
  }
}
