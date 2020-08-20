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
package net.consensys.besu.plugin.kinesis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.api.errors.SerializationException;
import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DefaultEvent;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class KinesisPublisherTest {

  @Mock private KinesisClient client;
  @Captor private ArgumentCaptor<PutRecordRequest> putRecordRequestCaptor;

  @Test
  @SuppressWarnings("MockitoInternalUsage")
  void publish() throws SerializationException {
    final TopicResolver topicResolver = new TopicResolver.Fixed(() -> "test-topic");
    final KinesisPublisher kinesisPublisher = new KinesisPublisher(client);

    final Event blockAddedEvent =
        DefaultEvent.create(
            "BlockAdded",
            "0xfe88c94d860f01a17f961bf4bdfb6e0c6cd10d3fda5cc861e805ca1240c58553",
            (mapper, payload) -> mapper.createObjectNode());
    when(client.putRecord(any(PutRecordRequest.class)))
        .thenReturn(PutRecordResponse.builder().build());
    kinesisPublisher.publish(DomainObjectType.BLOCK, topicResolver, blockAddedEvent);
    verify(client).putRecord(putRecordRequestCaptor.capture());
    assertThat(putRecordRequestCaptor.getValue().streamName()).isEqualTo("test-topic");
    assertThat(putRecordRequestCaptor.getValue().partitionKey())
        .isEqualTo(KinesisPluginConfiguration.DEFAULT_PARTITION_KEY);
    assertThat(putRecordRequestCaptor.getValue().data().asByteArray())
        .isEqualTo(blockAddedEvent.bytes());
  }
}
