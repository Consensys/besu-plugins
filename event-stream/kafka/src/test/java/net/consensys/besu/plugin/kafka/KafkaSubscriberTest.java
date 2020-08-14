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
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.apache.kafka.clients.consumer.KafkaConsumer;
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
public class KafkaSubscriberTest {

  @Mock private KafkaConsumer<String, String> consumer;
  @Captor private ArgumentCaptor<Collection<String>> topics;

  @Test
  void build() {
    assertThat(KafkaSubscriber.build(new KafkaPluginConfiguration())).isNotNull();
  }

  @Test
  void subscribe() {
    final KafkaSubscriber kafkaSubscriber = new KafkaSubscriber(consumer);
    assertThat(KafkaSubscriber.build(new KafkaPluginConfiguration())).isNotNull();
    kafkaSubscriber.subscribe("test", event -> {});
    verify(consumer).subscribe(topics.capture());
    assertThat(topics.getValue()).containsExactly("test");
  }
}
