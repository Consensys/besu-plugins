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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMqChannelFactory {
  private static final Logger LOGGER =
      LogManager.getLogger(net.consensys.besu.plugin.rabbitmq.RabbitMqSubscriber.class);

  public static Channel fromConfiguration(
      final net.consensys.besu.plugin.rabbitmq.RabbitMqPluginConfiguration configuration)
      throws Exception {
    final ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(configuration.getHost());
    factory.setPort(configuration.getPort());
    factory.setUsername(configuration.getUsername());
    factory.setPassword(configuration.getPassword());
    final Connection connection = factory.newConnection();
    final Channel channel = connection.createChannel();
    final AMQP.Queue.DeclareOk declareOk =
        channel.queueDeclare(configuration.getTopic(), true, false, false, null);
    LOGGER.debug(
        "Queue: {}, consumer count: {}, message count: {}",
        declareOk.getQueue(),
        declareOk.getConsumerCount(),
        declareOk.getMessageCount());
    return channel;
  }
}
