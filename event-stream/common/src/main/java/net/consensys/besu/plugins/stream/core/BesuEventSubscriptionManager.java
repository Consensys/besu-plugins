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

import net.consensys.besu.plugins.stream.model.BesuEventSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manager for subscriptions to Besu Events */
public class BesuEventSubscriptionManager {
  private static final Logger LOG = LogManager.getLogger();
  private final String name;
  private final BesuEventListener besuEventListener;

  private final List<BesuEventSubscription> subscriptions = new ArrayList<>();

  /**
   * Create an event subscription manager
   *
   * @param name the name to identify the subscription manager
   * @param besuEventListener the event listener for Besu events
   */
  public BesuEventSubscriptionManager(
      final String name, final BesuEventListener besuEventListener) {
    this.name = name;
    this.besuEventListener = besuEventListener;
  }

  /**
   * Add a subscription
   *
   * @param subscriberName the name of the subscriber
   * @param subscriber the function to invoke on subscribe
   * @param unsubscriber the function to invoke on unsubscribe
   * @return the event subscription manager
   */
  public BesuEventSubscriptionManager addSubscription(
      final String subscriberName,
      final Function<BesuEventListener, Long> subscriber,
      final Consumer<Long> unsubscriber) {
    subscriptions.add(BesuEventSubscription.of(subscriberName, subscriber, unsubscriber));
    return this;
  }

  /** start listening to all current subscriptions */
  public void subscribeAll() {
    subscriptions.forEach(
        subscription ->
            logListenerStart(
                subscription.getListenerName(),
                subscription.getSubscriber().apply(besuEventListener)));
  }

  /** stop listening to all current subscriptions */
  public void unsubscribeAll() {
    subscriptions.forEach(
        subscription ->
            subscription
                .getSubscriptionId()
                .ifPresent(
                    subscriptionId -> {
                      subscription.getUnsubscriber().accept(subscriptionId);
                      logListenerStop(subscription.getListenerName(), subscriptionId);
                    }));
  }

  /**
   * start the log listener for the given event type
   *
   * @param eventType the event type to start listening for
   * @param id identifier
   */
  private void logListenerStart(final String eventType, final long id) {
    LOG.info("Started listening for {} plugin for {} events with ID#{}", name, eventType, id);
  }

  /**
   * stop the log listener for the given event type
   *
   * @param eventType the event type to stop listening for
   * @param id identifier
   */
  private void logListenerStop(final String eventType, final long id) {
    LOG.info("Stopped listening for {} plugin for {} events with ID#{}", name, eventType, id);
  }

  @VisibleForTesting
  List<BesuEventSubscription> getSubscriptions() {
    return subscriptions;
  }
}
