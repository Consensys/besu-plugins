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

import net.consensys.besu.plugins.stream.api.event.EventHandler;
import net.consensys.besu.plugins.stream.api.event.Subscriber;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to consume event objects to rabbitmq stream.
 *
 * @since 0.1
 */
public class RabbitMqSubscriber implements Subscriber {
  private static final Logger LOGGER = LogManager.getLogger(RabbitMqSubscriber.class);
  private final Channel channel;

  public RabbitMqSubscriber(final Channel channel) {
    this.channel = channel;
  }

  /**
   * Build a {@link RabbitMqSubscriber} instance from specified configuration.
   *
   * @param configuration The configuration to apply.
   * @return An instance of {@link RabbitMqSubscriber}.
   */
  public static Subscriber build(final RabbitMqPluginConfiguration configuration) {
    try {
      return new RabbitMqSubscriber(RabbitMqChannelFactory.fromConfiguration(configuration));
    } catch (Exception e) {
      throw new RuntimeException("Can't instantiate RabbitMq plugin.", e);
    }
  }

  @Override
  public void subscribe(String topic, EventHandler eventHandler) {
    try {
      channel.basicConsume(topic, new RabbitMqConsumer(channel, eventHandler));
    } catch (IOException e) {
      LOGGER.error("Cannot subscribe to topic.", e);
    }
  }
}
