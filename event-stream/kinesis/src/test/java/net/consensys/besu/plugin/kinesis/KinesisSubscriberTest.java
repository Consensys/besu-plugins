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
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.Consumer;
import software.amazon.awssdk.services.kinesis.model.RegisterStreamConsumerRequest;
import software.amazon.awssdk.services.kinesis.model.RegisterStreamConsumerResponse;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardResponseHandler;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class KinesisSubscriberTest {

  private Consumer consumer;
  @Mock private KinesisAsyncClient client;
  @Captor private ArgumentCaptor<SubscribeToShardRequest> subscribeToShardRequestArgumentCaptor;

  @Test
  void subscribe() {
    final KinesisSubscriber kinesisSubscriber =
        new KinesisSubscriber(client, "consumerARN", "consumerName");
    consumer = Consumer.builder().consumerARN("consumerARN").build();
    final RegisterStreamConsumerResponse registerStreamConsumerResponse =
        RegisterStreamConsumerResponse.builder().consumer(consumer).build();
    when(client.registerStreamConsumer(any(RegisterStreamConsumerRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(registerStreamConsumerResponse));
    when(client.subscribeToShard(
            subscribeToShardRequestArgumentCaptor.capture(),
            any(SubscribeToShardResponseHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    ;
    kinesisSubscriber.subscribe("", event -> {});
    assertThat(subscribeToShardRequestArgumentCaptor.getValue().consumerARN())
        .isEqualTo("consumerARN");
  }
}
