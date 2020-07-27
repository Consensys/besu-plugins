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

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.util.Objects;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to publish event objects to kafka stream.
 *
 * @since 0.1
 */
public class KafkaPublisher implements Publisher {
  private static final Logger LOGGER = LogManager.getLogger(KafkaPublisher.class);
  private final KafkaProducer<String, String> producer;

  public KafkaPublisher(final KafkaProducer<String, String> producer) {
    this.producer = producer;
  }

  /**
   * Build a {@link KafkaPublisher} instance from specified configuration.
   *
   * @param pluginConfiguration The configuration to apply.
   * @return An instance of {@link KafkaPublisher}.
   */
  public static Publisher build(final KafkaPluginConfiguration pluginConfiguration) {
    try {
      // This enables to load the org.apache.kafka.common.security.plain.PlainLoginModule.
      // When starting by Besu the plugin fails to build a KafkaProducer without this line.
      Thread.currentThread().setContextClassLoader(KafkaPublisher.class.getClassLoader());
      return new KafkaPublisher(new KafkaProducer<>(pluginConfiguration.properties()));
    } catch (Throwable e) {
      e.printStackTrace();
      LOGGER.error(e);
      throw new RuntimeException("Can't instantiate Kafka plugin.", e);
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
    LOGGER.debug("Publishing in topic: {}", topic);
    LOGGER.debug("Publishing event: {}", event.string());
    producer.send(new ProducerRecord<>(topic, event.string()), this::onCompletion);
  }

  private void onCompletion(final RecordMetadata metadata, final Exception exception) {
    if (Objects.isNull(exception)) {
      LOGGER.debug(
          "Record sent in topic {} to partition {} with offset {}.",
          metadata.topic(),
          metadata.partition(),
          metadata.offset());
    } else {
      LOGGER.error("Error occurred while publishing message.", exception);
    }
  }
}
