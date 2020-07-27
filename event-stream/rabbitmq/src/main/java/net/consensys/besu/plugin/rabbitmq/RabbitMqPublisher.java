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
package net.consensys.besu.plugin.rabbitmq;

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to publish event objects to rabbitmq queue.
 *
 * @since 0.1
 */
public class RabbitMqPublisher implements Publisher {
  private static final Logger LOGGER = LogManager.getLogger(RabbitMqPublisher.class);
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private final Channel channel;

  private RabbitMqPublisher(final Channel channel) {
    this.channel = channel;
  }

  /**
   * Build a {@link RabbitMqPublisher} instance from specified configuration.
   *
   * @param configuration The configuration to apply.
   * @return An instance of {@link RabbitMqPublisher}.
   */
  public static Publisher build(final RabbitMqPluginConfiguration configuration) {
    try {
      return new RabbitMqPublisher(RabbitMqChannelFactory.fromConfiguration(configuration));
    } catch (Exception e) {
      throw new RuntimeException("Can't instantiate RabbitMq plugin.", e);
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
    try {
      final String topic = topicResolver.resolve(domainObjectType, event);
      final String eventString = event.string();
      LOGGER.debug("Publishing in topic: {}", topic);
      LOGGER.debug("Publishing event: {}", eventString);
      channel.basicPublish("", topic, null, eventString.getBytes(CHARSET));
    } catch (IOException e) {
      LOGGER.error("Cannot publish message.", e);
    }
  }
}
