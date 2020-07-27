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

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toUnmodifiableList;

import net.consensys.besu.plugins.types.Address;
import net.consensys.besu.plugins.types.Hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.google.common.annotations.VisibleForTesting;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

public class EventSchema {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private final String id;
  private final Address contractAddress;
  private final String eventName;

  @SuppressWarnings("rawtypes")
  private final List<TypeReference<Type>> parameterTypes;

  private final Hash topic;

  @VisibleForTesting
  public EventSchema(
      final String id,
      final Address contractAddress,
      final String eventName,
      @SuppressWarnings("rawtypes") final List<TypeReference<Type>> parameterTypes) {
    this.id = id;
    this.contractAddress = contractAddress;
    this.eventName = eventName;
    this.parameterTypes = parameterTypes;
    this.topic = topic();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected static EventSchema from(final SingleEventParseTarget parseTarget) {
    final List<ParameterDefinition> indexedParameterDefinitions =
        parseTarget.getEventSpecification().getIndexedParameterDefinitions();
    final List<ParameterDefinition> nonIndexedParameterDefinitions =
        parseTarget.getEventSpecification().getNonIndexedParameterDefinitions();
    final List<TypeReference<Type>> parameterTypes =
        new ArrayList<>(indexedParameterDefinitions.size() + nonIndexedParameterDefinitions.size());

    indexedParameterDefinitions.forEach(
        parameterDefinition -> {
          try {
            parameterTypes.add(
                parameterDefinition.getPosition(),
                TypeReference.makeTypeReference(parameterDefinition.getType(), true, true));
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        });

    nonIndexedParameterDefinitions.forEach(
        parameterDefinition -> {
          try {
            parameterTypes.add(
                parameterDefinition.getPosition(),
                TypeReference.makeTypeReference(parameterDefinition.getType(), false, true));
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        });

    final int nullIndex = parameterTypes.indexOf(null);
    final String eventName = parseTarget.getEventSpecification().getEventName();
    checkState(
        nullIndex == -1,
        String.format(
            "Missing parameter definition at index %d for contract %s", nullIndex, eventName));

    return new EventSchema(
        parseTarget.getId(),
        Address.fromHexString(parseTarget.getContractAddress()),
        eventName,
        parameterTypes);
  }

  @SuppressWarnings("rawtypes")
  private Hash topic() {
    try {
      final MessageDigest digest = MessageDigest.getInstance("KECCAK-256");
      return Hash.wrap(
          digest.digest(
              String.format(
                      "%s%s",
                      eventName,
                      parameterTypes.stream()
                          // get the solidity type name from the parameter types
                          .map(
                              typeReference -> {
                                try {
                                  return typeReference.getClassType();
                                } catch (ClassNotFoundException e) {
                                  throw new RuntimeException(e);
                                }
                              })
                          .map(Class::getSimpleName)
                          .map(String::toLowerCase)
                          .map(typeName -> typeName.replaceFirst("^utf8", ""))
                          .collect(toUnmodifiableList())
                          .toString()
                          // reformat the java toString output to be in the form that is expected by
                          // the keccak hash
                          .replace(" ", "")
                          .toLowerCase()
                          .replace('[', '(')
                          .replace(']', ')'))
                  .getBytes(UTF_8)));
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public String getId() {
    return id;
  }

  public Address getContractAddress() {
    return contractAddress;
  }

  public String getEventName() {
    return eventName;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    final EventSchema that = (EventSchema) other;
    return Objects.equals(id, that.id)
        && Objects.equals(contractAddress, that.contractAddress)
        && Objects.equals(eventName, that.eventName)
        && IntStream.range(0, parameterTypes.size())
            .allMatch(
                i ->
                    that.getParameterTypes()
                        .get(i)
                        .getClass()
                        .equals(parameterTypes.get(i).getClass()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, contractAddress, eventName);
  }

  public Hash getTopic() {
    return topic;
  }

  @SuppressWarnings("rawtypes")
  public List<TypeReference<Type>> getParameterTypes() {
    return parameterTypes;
  }
}
