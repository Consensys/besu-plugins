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
package net.consensys.besu.plugins.stream.model;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import net.consensys.besu.plugins.stream.api.errors.SerializationException;
import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.EventSerializer;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Super class of all Ethereum client events. This class generates some fields automatically. */
public class DefaultEvent<T> implements Event {
  private final String type;
  private final String uuid;
  private final long timestamp;
  private final T event;
  private String rawJSONPayload;

  /**
   * Creates a {@link DefaultEvent} object using the specified serializer.
   *
   * @param <T> type of payload object
   * @param type event type
   * @param event event payload object
   * @param serializer the {@link EventSerializer} to use
   * @return DefaultEvent object
   * @throws SerializationException when JsonProcessingException is caught
   */
  public static <T> DefaultEvent<T> create(
      final String type, final T event, final EventSerializer<T> serializer)
      throws SerializationException {
    final DefaultEvent<T> defaultEvent = new DefaultEvent<>(type, event);
    try {
      final ObjectMapper mapper = new ObjectMapper();
      final ObjectNode rootNode = mapper.createObjectNode();
      rootNode
          .put("uuid", defaultEvent.uuid)
          .put("type", type)
          .put("timestamp", defaultEvent.timestamp);
      final JsonNode eventNode = serializer.serialize(mapper, event);
      rootNode.set("event", eventNode);
      defaultEvent.rawJSONPayload = mapper.writeValueAsString(rootNode);
    } catch (JsonProcessingException e) {
      throw new SerializationException(e);
    }
    return defaultEvent;
  }

  /**
   * @param uuid event identifier
   * @param type event type
   * @param event event payload object
   */
  private DefaultEvent(final String uuid, final String type, final T event) {
    this.uuid = uuid;
    this.timestamp = now().toEpochMilli();
    this.type = type;
    this.event = event;
  }

  /**
   * @param type event type
   * @param event event payload object
   */
  private DefaultEvent(final String type, final T event) {
    this(randomUUID().toString(), type, event);
  }

  /**
   * Convert object instance as JSON string.
   *
   * @return The JSON string representing the object instance.
   */
  @Override
  public String string() {
    return rawJSONPayload;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    if (other instanceof DefaultEvent<?>) {
      final DefaultEvent<?> that = (DefaultEvent<?>) other;
      return timestamp == that.timestamp
          && Objects.equals(uuid, that.uuid)
          && Objects.equals(type, that.type)
          && Objects.equals(event, that.event);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, timestamp);
  }

  @Override
  public String type() {
    return getType();
  }

  public String getType() {
    return type;
  }

  public String getUuid() {
    return uuid;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public T getEvent() {
    return event;
  }
}
