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

import net.consensys.besu.plugins.stream.api.event.EventHandler;
import net.consensys.besu.plugins.stream.api.event.Subscriber;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NatsSubscriber implements Subscriber {

  private static final Logger LOGGER = LogManager.getLogger(NatsSubscriber.class);
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private Optional<Connection> maybeConnection;
  private final Map<String, EventHandler> handlerSubscriptions;

  public NatsSubscriber(final Optional<Connection> maybeConnection) {
    this.maybeConnection = maybeConnection;
    this.handlerSubscriptions = new HashMap<>();
  }
  /**
   * Build a {@link NatsSubscriber} instance from specified configuration.
   *
   * @param pluginConfiguration The configuration to apply.
   * @return An instance of {@link NatsSubscriber}.
   */
  public static Subscriber build(final NatsPluginConfiguration pluginConfiguration) {
    try {
      final NatsSubscriber natsSubscriber = new NatsSubscriber(Optional.empty());
      Nats.connectAsynchronously(
          new Options.Builder()
              .server(pluginConfiguration.getBrokerUrl())
              .connectionListener(natsSubscriber::connectionEvent)
              .build(),
          true);
      return natsSubscriber;
    } catch (InterruptedException e) {
      throw new RuntimeException("Can't instantiate Nats plugin.", e);
    }
  }

  private void connectionEvent(final Connection connection, final ConnectionListener.Events type) {
    LOGGER.debug("Connection event: {}", type.toString());
    if (ConnectionListener.Events.CONNECTED.equals(type)) {
      onConnected(connection);
    }
  }

  private void onConnected(final Connection connection) {
    this.maybeConnection = Optional.of(connection);
    LOGGER.debug("Register pending subscriptions.");
    handlerSubscriptions.forEach(
        (topic, eventHandler) -> doSubscribe(connection, topic, eventHandler));
  }

  @Override
  public void subscribe(final String topic, final EventHandler eventHandler) {
    maybeConnection.ifPresentOrElse(
        connection -> doSubscribe(connection, topic, eventHandler),
        () -> handlerSubscriptions.put(topic, eventHandler));
  }

  private static void doSubscribe(
      final Connection connection, final String topic, final EventHandler eventHandler) {
    final Dispatcher dispatcher =
        connection.createDispatcher(msg -> eventHandler.apply(new String(msg.getData(), CHARSET)));
    dispatcher.subscribe(topic);
  }
}
