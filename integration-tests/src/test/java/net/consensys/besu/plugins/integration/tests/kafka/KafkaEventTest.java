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
package net.consensys.besu.plugins.integration.tests.kafka;

import static java.util.concurrent.TimeUnit.SECONDS;

import net.consensys.besu.plugins.stream.api.event.Event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class KafkaEventTest {

  /**
   * This test checks that Besu can communicate with the kafka plugin by checking that events are
   * sent. This test requires a properly configured kafka and Besu. it is used in the nightly
   */
  @Test
  @Timeout(value = 60, unit = SECONDS)
  public void isBlockPropagatedEventPublished() throws ClassNotFoundException {

    final Properties props = new Properties();
    props.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("TEST_BOOTSTRAP_SERVERS_CONFIG"));
    props.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "besu-consumer-group");
    // Create the consumer using props.
    final Consumer<String, String> consumer = new KafkaConsumer<>(props);
    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList(System.getenv("TOPIC")));

    // polling
    final ArrayList<String> eventsToCheck = new ArrayList<>();
    eventsToCheck.add(Event.Type.BLOCK_ADDED);
    eventsToCheck.add(Event.Type.BLOCK_PROPAGATED);

    while (!eventsToCheck.isEmpty()) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
      for (ConsumerRecord<String, String> record : records) {
        final Optional<String> eventFound =
            eventsToCheck.stream().filter(event -> record.value().contains(event)).findFirst();
        eventFound.ifPresent(eventsToCheck::remove);
      }
    }
  }
}
