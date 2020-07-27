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
package net.consensys.besu.plugins.stream.model;

import net.consensys.besu.plugins.stream.core.BesuEventListener;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class BesuEventSubscription {
  private final String listenerName;
  private final Function<BesuEventListener, Long> subscriber;
  private final Consumer<Long> unsubscriber;

  private Optional<Long> subscriptionId = Optional.empty();

  private BesuEventSubscription(
      final String subscriberName,
      final Function<BesuEventListener, Long> subscriber,
      final Consumer<Long> unsubscriber) {
    this.listenerName = subscriberName;
    this.subscriber =
        pluginEventListener -> {
          subscriptionId = Optional.of(subscriber.apply(pluginEventListener));
          return subscriptionId.get();
        };
    this.unsubscriber = unsubscriber;
  }

  public static BesuEventSubscription of(
      final String listenerName,
      final Function<BesuEventListener, Long> subscriber,
      final Consumer<Long> remover) {
    return new BesuEventSubscription(listenerName, subscriber, remover);
  }

  public String getListenerName() {
    return listenerName;
  }

  public Function<BesuEventListener, Long> getSubscriber() {
    return subscriber;
  }

  public Consumer<Long> getUnsubscriber() {
    return unsubscriber;
  }

  public Optional<Long> getSubscriptionId() {
    return subscriptionId;
  }
}
