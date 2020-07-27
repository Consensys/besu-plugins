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
package net.consensys.besu.plugin.nats;

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to publish event objects to nats stream.
 *
 * @since 0.1
 */
public class NatsPublisher implements Publisher {
  private static final Logger LOGGER = LogManager.getLogger(NatsPublisher.class);
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private final Connection nc;

  public NatsPublisher(final Connection nc) {
    this.nc = nc;
  }

  /**
   * Build a {@link NatsPublisher} instance from specified configuration.
   *
   * @param pluginConfiguration The configuration to apply.
   * @return An instance of {@link NatsPublisher}.
   */
  public static Publisher build(final NatsPluginConfiguration pluginConfiguration) {
    try {
      final Connection nc = Nats.connect(pluginConfiguration.getBrokerUrl());
      return new NatsPublisher(nc);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Can't instantiate Nats plugin.", e);
    }
  }

  /**
   * Publish an event to a kafka stream.
   *
   * @param event The event to publish.
   */
  @Override
  public void publish(
      final DomainObjectType domainObjectType,
      final TopicResolver topicResolver,
      final Event event) {
    final String topic = topicResolver.resolve(domainObjectType, event);
    final String eventString = event.string();
    LOGGER.debug("Publishing in topic: {}", topic);
    LOGGER.debug("Publishing event: {}", eventString);
    nc.publish(topic, eventString.getBytes(CHARSET));
  }
}
