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

import net.consensys.besu.plugins.types.Address;
import net.consensys.besu.plugins.types.BigIntegerQuantity;
import net.consensys.besu.plugins.types.Fixture;

import java.math.BigInteger;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.data.Hash;
import org.hyperledger.besu.plugin.data.Transaction;

public final class TransactionMockFixture {

  public static Transaction createTransactionMock() {
    final Transaction transactionMock = mock(Transaction.class);
    final Address senderMock = mock(Address.class);
    final Hash hashMock = mock(Hash.class);

    when(senderMock.toHexString()).thenReturn(Fixture.ADDRESS);
    when(hashMock.toHexString()).thenReturn(Fixture.HASH);

    when(transactionMock.getNonce()).thenReturn(1L);
    when(transactionMock.getGasPrice())
        .thenAnswer(invocation -> Optional.of(new BigIntegerQuantity(BigInteger.valueOf(0x7d0))));
    when(transactionMock.getGasLimit()).thenReturn(3L);
    when(transactionMock.getTo()).thenReturn(Optional.empty());
    when(transactionMock.getValue()).thenReturn(new BigIntegerQuantity(BigInteger.valueOf(0)));
    when(transactionMock.getV()).thenReturn(BigInteger.valueOf(5L));
    when(transactionMock.getR()).thenReturn(BigInteger.valueOf(6L));
    when(transactionMock.getS()).thenReturn(BigInteger.valueOf(7L));
    when(transactionMock.getSender()).thenReturn(senderMock);
    when(transactionMock.getChainId()).thenReturn(Optional.empty());
    when(transactionMock.getHash()).thenReturn(hashMock);
    when(transactionMock.getPayload()).thenReturn(Bytes.fromHexString("1234"));

    return transactionMock;
  }
}
