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
package net.consensys.besu.plugins.stream.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class BesuEventSubscriptionManagerTest {

  @Mock private BesuEventListener besuEventListenerMock;
  @Mock private Function<BesuEventListener, Long> subscriberMock;
  @Mock private Consumer<Long> unsubscriberMock;

  @Test
  void assertAddSubscriptionCorrectlyAddsEntry() {
    final BesuEventSubscriptionManager subscriptionManager =
        new BesuEventSubscriptionManager("besu", besuEventListenerMock);
    assertThat(subscriptionManager.getSubscriptions()).hasSize(0);
    assertThat(
            subscriptionManager.addSubscription(
                "test-subscriber", pluginEventListener -> 0L, id -> {}))
        .isSameAs(subscriptionManager);
    assertThat(subscriptionManager.getSubscriptions()).hasSize(1);
  }

  @Test
  void assertSubscribeCallsSubscriberFunctionForAllSubscriptions() {
    final BesuEventSubscriptionManager subscriptionManager =
        new BesuEventSubscriptionManager("besu", besuEventListenerMock);
    when(subscriberMock.apply(besuEventListenerMock)).thenReturn(5L);
    subscriptionManager
        .addSubscription("test-subscriber-1", subscriberMock, id -> {})
        .addSubscription("test-subscriber-2", subscriberMock, id -> {})
        .subscribeAll();
    verify(subscriberMock, times(2)).apply(besuEventListenerMock);
  }

  @Test
  void assertUnsubscribeCallsUnsubscribeFunctionForAllSubscriptions() {
    final BesuEventSubscriptionManager subscriptionManager =
        new BesuEventSubscriptionManager("besu", besuEventListenerMock);
    when(subscriberMock.apply(besuEventListenerMock)).thenReturn(49L, 13L);
    subscriptionManager
        .addSubscription("test-subscriber-1", subscriberMock, unsubscriberMock)
        .addSubscription("test-subscriber-2", subscriberMock, unsubscriberMock);
    subscriptionManager.subscribeAll();
    subscriptionManager.unsubscribeAll();
    verify(unsubscriberMock).accept(49L);
    verify(unsubscriberMock).accept(13L);
  }
}
