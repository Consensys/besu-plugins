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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DefaultEvent;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@SuppressWarnings("rawtypes")
public class KafkaPublisherTest {
  @Mock private KafkaProducer<String, String> producer;
  @Captor private ArgumentCaptor<ProducerRecord> record;

  @Test
  void build() {
    final Publisher kafkaPublisher = KafkaPublisher.build(new KafkaPluginConfiguration());
    assertNotNull(kafkaPublisher);
  }

  @Test
  @SuppressWarnings({"MockitoInternalUsage", "unchecked"})
  void publish() throws Exception {
    final Publisher kafkaPublisher = new KafkaPublisher(producer);
    final Event blockAddedEvent =
        DefaultEvent.create(
            "BlockAdded",
            "0xfe88c94d860f01a17f961bf4bdfb6e0c6cd10d3fda5cc861e805ca1240c58553",
            (mapper, payload) -> mapper.createObjectNode());
    kafkaPublisher.publish(
        DomainObjectType.BLOCK, new TopicResolver.Fixed(() -> "test-topic"), blockAddedEvent);
    verify(producer).send(record.capture(), any());
    assertThat(record.getValue().value()).isEqualTo(blockAddedEvent.string());
  }
}
