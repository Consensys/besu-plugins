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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.core.config.EventSchema;
import net.consensys.besu.plugins.stream.core.config.EventSchemas;
import net.consensys.besu.plugins.types.Address;
import net.consensys.besu.plugins.types.DecodedLogWithMetadata;
import net.consensys.besu.plugins.types.Hash;

import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.LogWithMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.abi.TypeReference;

@ExtendWith(MockitoExtension.class)
public class LogDecoderTest {
  private static final String DECODED =
      "Transfer(0xc2ca8977e5c582f938c30f7a5328ac1d101bd564,0x06e01587ad6b033d4b05156003c3412e8997bfe3,801976598137298011761447)";
  private static final String ANON_DECODED =
      "(0xc2ca8977e5c582f938c30f7a5328ac1d101bd564,0x06e01587ad6b033d4b05156003c3412e8997bfe3,801976598137298011761447)";
  private static final String ADDRESS = "0x1dea979ae76f26071870f824088da78979eb91c8";
  private static final String LOG_DATA =
      "00000000000000000000000000000000000000000000a9d33d0e47bfd6c72b27";

  @Mock EventSchemas eventSchemas;

  @Mock LogWithMetadata logWithMetadata;

  @Test
  public void testDecode() throws ClassNotFoundException {
    doReturn(
            List.of(
                    "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                    "0x000000000000000000000000c2ca8977e5c582f938c30f7a5328ac1d101bd564",
                    "0x00000000000000000000000006e01587ad6b033d4b05156003c3412e8997bfe3")
                .stream()
                .map(Hash::fromHexString)
                .collect(toUnmodifiableList()))
        .when(logWithMetadata)
        .getTopics();
    when(logWithMetadata.getLogger()).thenReturn(Address.fromHexString(ADDRESS));
    when(eventSchemas.getSchemas()).thenReturn(createSchemas());
    when(logWithMetadata.getData()).thenReturn(Bytes.fromHexStringLenient(LOG_DATA));

    final LogWithMetadata result = LogDecoder.decode(eventSchemas, logWithMetadata);
    final DecodedLogWithMetadata expectedResult =
        new DecodedLogWithMetadata(logWithMetadata, DECODED);
    Assertions.assertEquals(expectedResult.getClass(), result.getClass());
    Assertions.assertAll(
        () -> Assertions.assertEquals(expectedResult.getBlockHash(), result.getBlockHash()),
        () -> Assertions.assertEquals(expectedResult.getBlockNumber(), result.getBlockNumber()),
        () -> Assertions.assertEquals(expectedResult.getData(), result.getData()),
        () ->
            Assertions.assertEquals(
                expectedResult.getDecoded(), ((DecodedLogWithMetadata) result).getDecoded()),
        () -> Assertions.assertEquals(expectedResult.getLogger(), result.getLogger()),
        () -> Assertions.assertEquals(expectedResult.getLogIndex(), result.getLogIndex()),
        () -> Assertions.assertEquals(expectedResult.getTopics(), result.getTopics()),
        () ->
            Assertions.assertEquals(
                expectedResult.getTransactionHash(), result.getTransactionHash()),
        () ->
            Assertions.assertEquals(
                expectedResult.getTransactionIndex(), result.getTransactionIndex()));
  }

  @Test
  public void testDecodeAnonymous() throws ClassNotFoundException {
    doReturn(
            List.of(
                    "0x000000000000000000000000c2ca8977e5c582f938c30f7a5328ac1d101bd564",
                    "0x00000000000000000000000006e01587ad6b033d4b05156003c3412e8997bfe3")
                .stream()
                .map(Hash::fromHexString)
                .collect(toUnmodifiableList()))
        .when(logWithMetadata)
        .getTopics();
    when(logWithMetadata.getLogger()).thenReturn(Address.fromHexString(ADDRESS));
    when(eventSchemas.getSchemas()).thenReturn(createSchemas());
    when(logWithMetadata.getData()).thenReturn(Bytes.fromHexStringLenient(LOG_DATA));

    final LogWithMetadata result = LogDecoder.decode(eventSchemas, logWithMetadata);
    final DecodedLogWithMetadata expectedResult =
        new DecodedLogWithMetadata(logWithMetadata, ANON_DECODED);
    Assertions.assertEquals(expectedResult.getClass(), result.getClass());
    Assertions.assertAll(
        () -> Assertions.assertEquals(expectedResult.getBlockHash(), result.getBlockHash()),
        () -> Assertions.assertEquals(expectedResult.getBlockNumber(), result.getBlockNumber()),
        () -> Assertions.assertEquals(expectedResult.getData(), result.getData()),
        () ->
            Assertions.assertEquals(
                expectedResult.getDecoded(), ((DecodedLogWithMetadata) result).getDecoded()),
        () -> Assertions.assertEquals(expectedResult.getLogger(), result.getLogger()),
        () -> Assertions.assertEquals(expectedResult.getLogIndex(), result.getLogIndex()),
        () -> Assertions.assertEquals(expectedResult.getTopics(), result.getTopics()),
        () ->
            Assertions.assertEquals(
                expectedResult.getTransactionHash(), result.getTransactionHash()),
        () ->
            Assertions.assertEquals(
                expectedResult.getTransactionIndex(), result.getTransactionIndex()));
  }

  @Test
  public void testDecodeNoTopics() {
    when(logWithMetadata.getTopics()).thenReturn(null);
    final LogWithMetadata result = LogDecoder.decode(eventSchemas, logWithMetadata);

    Assertions.assertEquals(logWithMetadata, result);
  }

  @Test()
  public void testDecodeTooManyMatches() throws ClassNotFoundException {
    doReturn(
            List.of(
                    "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                    "0x00000000000000000000000006e01587ad6b033d4b05156003c3412e8997bfe3")
                .stream()
                .map(Hash::fromHexString)
                .collect(toUnmodifiableList()))
        .when(logWithMetadata)
        .getTopics();
    when(logWithMetadata.getLogger()).thenReturn(Address.fromHexString(ADDRESS));
    when(eventSchemas.getSchemas()).thenReturn(createDupSchemas());

    Assertions.assertThrows(
        IllegalStateException.class, () -> LogDecoder.decode(eventSchemas, logWithMetadata));
  }

  @SuppressWarnings("unchecked")
  private Set<EventSchema> createSchemas() throws ClassNotFoundException {
    return Set.of(
        new EventSchema(
            "randomRealLog",
            Address.fromHexString(ADDRESS),
            "Transfer",
            List.of(
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("uint256", false, true))),
        new EventSchema(
            "AnonymousLog",
            Address.fromHexString(ADDRESS),
            null,
            List.of(
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("uint256", false, true))));
  }

  @SuppressWarnings("unchecked")
  private Set<EventSchema> createDupSchemas() throws ClassNotFoundException {
    return Set.of(
        new EventSchema(
            "randomRealLog",
            Address.fromHexString(ADDRESS),
            "Transfer",
            List.of(
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("uint256", false, true))),
        new EventSchema(
            "randomRealLog2",
            Address.fromHexString(ADDRESS),
            "Transfer",
            List.of(
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("address", true, true),
                TypeReference.makeTypeReference("uint256", false, true))));
  }
}
