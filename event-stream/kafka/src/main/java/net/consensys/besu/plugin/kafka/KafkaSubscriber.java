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
package net.consensys.besu.plugin.kafka;

import net.consensys.besu.plugins.stream.api.event.EventHandler;
import net.consensys.besu.plugins.stream.api.event.Subscriber;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaSubscriber implements Subscriber {

  private static final Logger LOGGER = LogManager.getLogger(KafkaSubscriber.class);
  private final KafkaConsumer<String, String> consumer;
  private final Duration pollTimeout = Duration.ofMillis(10);
  private final long pollIntervalMillis = 100;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public KafkaSubscriber(final KafkaConsumer<String, String> consumer) {
    this.consumer = consumer;
  }

  /**
   * Build a {@link KafkaSubscriber} instance from specified configuration.
   *
   * @param pluginConfiguration The configuration to apply.
   * @return An instance of {@link KafkaSubscriber}.
   */
  public static Subscriber build(final KafkaPluginConfiguration pluginConfiguration) {
    try {
      final Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, pluginConfiguration.getBrokerUrl());
      props.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
      props.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "besu-consumer-group");

      return new KafkaSubscriber(new KafkaConsumer<>(props));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Can't instantiate Kafka plugin.", e);
    }
  }

  @Override
  public void subscribe(final String topic, final EventHandler eventHandler) {
    consumer.subscribe(Collections.singleton(topic));
    executor.scheduleAtFixedRate(
        () -> apply(eventHandler, consumer.poll(pollTimeout)),
        0,
        pollIntervalMillis,
        TimeUnit.MILLISECONDS);
  }

  private void apply(
      final EventHandler eventHandler, final ConsumerRecords<String, String> records) {
    records.forEach(
        (record) -> {
          LOGGER.debug(
              "Received message on topic {} at offset {}: {}",
              record.topic(),
              record.offset(),
              record.value());
          eventHandler.apply(record.value());
        });
  }
}
