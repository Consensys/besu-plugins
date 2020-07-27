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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class NatsSubscriberTest {

  @Mock private Connection connectionMock;
  @Captor private ArgumentCaptor<MessageHandler> messageHandlerCaptor;

  @Test
  void build() {
    assertThat(NatsSubscriber.build(new NatsPluginConfiguration())).isNotNull();
  }

  @Test
  void subscribe() {
    final NatsSubscriber natsSubscriber = new NatsSubscriber(Optional.of(connectionMock));
    assertThat(NatsSubscriber.build(new NatsPluginConfiguration())).isNotNull();
    final Dispatcher dispatcher = Mockito.mock(Dispatcher.class);
    when(connectionMock.createDispatcher(any(MessageHandler.class))).thenReturn(dispatcher);
    natsSubscriber.subscribe("test", event -> {});
    verify(connectionMock).createDispatcher(messageHandlerCaptor.capture());
  }
}
