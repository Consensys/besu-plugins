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
package net.consensys.besu.plugins.stream.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.types.Fixture;

import java.math.BigInteger;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.Quantity;

public final class BlockHeaderMockFixture {

  public static BlockHeader createBlockHeaderMock(final long timestamp) {
    final BlockHeader blockHeaderMock = mock(BlockHeader.class);
    final Hash hashMock = mock(Hash.class);
    final Address addressMock = mock(Address.class);
    final Quantity quantityMock = mock(Quantity.class);
    when(blockHeaderMock.getBlockHash()).thenReturn(hashMock);
    when(blockHeaderMock.getNumber()).thenReturn(1L);
    when(blockHeaderMock.getNonce()).thenReturn(3L);
    when(blockHeaderMock.getDifficulty()).thenReturn(quantityMock);
    when(blockHeaderMock.getGasLimit()).thenReturn(5L);
    when(blockHeaderMock.getGasUsed()).thenReturn(2L);
    when(blockHeaderMock.getTimestamp()).thenReturn(timestamp);
    when(blockHeaderMock.getCoinbase()).thenReturn(addressMock);
    when(blockHeaderMock.getExtraData()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getLogsBloom()).thenReturn(Bytes.EMPTY);
    when(blockHeaderMock.getMixHash()).thenReturn(hashMock);
    when(blockHeaderMock.getOmmersHash()).thenReturn(hashMock);
    when(blockHeaderMock.getParentHash()).thenReturn(hashMock);
    when(blockHeaderMock.getReceiptsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getTransactionsRoot()).thenReturn(hashMock);
    when(blockHeaderMock.getStateRoot()).thenReturn(hashMock);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);

    when(hashMock.toHexString()).thenReturn(Fixture.HASH);
    when(addressMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(quantityMock.getValue()).thenReturn(BigInteger.ONE);

    return blockHeaderMock;
  }
}
