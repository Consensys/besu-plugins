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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DefaultEvent;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.nio.charset.StandardCharsets;

import io.nats.client.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class NatsPublisherTest {

  @Mock private Connection connectionMock;

  @Test
  @SuppressWarnings("MockitoInternalUsage")
  void publish() throws Exception {
    final Publisher natsPublisher = new NatsPublisher(connectionMock);
    final Event blockAddedEvent =
        DefaultEvent.create(
            "BlockAdded",
            "0xfe88c94d860f01a17f961bf4bdfb6e0c6cd10d3fda5cc861e805ca1240c58553",
            (mapper, payload) -> mapper.createObjectNode());
    natsPublisher.publish(
        DomainObjectType.BLOCK, new TopicResolver.Fixed(() -> "test-topic"), blockAddedEvent);
    verify(connectionMock)
        .publish(eq("test-topic"), eq(blockAddedEvent.string().getBytes(StandardCharsets.UTF_8)));
  }
}
