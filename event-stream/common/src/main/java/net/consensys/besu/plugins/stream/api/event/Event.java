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
package net.consensys.besu.plugins.stream.api.event;

import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;

/**
 * This interface represents any Ethereum client event.
 *
 * @since 0.1
 */
public interface Event {

  /**
   * The type
   *
   * @return A string representing the type of the event
   */
  String type();

  /**
   * Returns the string representation of the event.
   *
   * @return A string representation of the event.
   */
  String string();

  /**
   * Returns a byte array corresponding to the UTF-8 string representation of the event.
   *
   * @return A byte array corresponding to the UTF-8 string representation of the event.
   */
  default byte[] bytes() {
    return string().getBytes(UTF_8);
  }

  /**
   * Returns a {@link ByteBuffer} used by KPL library to publish in kinesis stream.
   *
   * @return A {@link ByteBuffer} used by KPL library to publish in kinesis stream.
   */
  default ByteBuffer buffer() {
    return wrap(bytes());
  }

  /** event types */
  class Type {
    /** block propagated */
    public static final String BLOCK_PROPAGATED = "BlockPropagated";
    /** block added */
    public static final String BLOCK_ADDED = "BlockAdded";
    /** block reorg */
    public static final String BLOCK_REORG = "BlockReorg";
    /** transaction added */
    public static final String TRANSACTION_ADDED = "TransactionAdded";
    /** transaction dropped */
    public static final String TRANSACTION_DROPPED = "TransactionDropped";
    /** transaction reverted */
    public static final String TRANSACTION_REVERTED = "TransactionReverted";
    /** sync status changed. Start or stop syncing. */
    public static final String SYNC_STATUS_CHANGED = "SyncStatusChanged";
    /** log emitted */
    public static final String LOG_EMITTED = "LogEmitted";
  }
}
