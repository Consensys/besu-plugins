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

import static java.util.stream.Collectors.toUnmodifiableList;
import static net.consensys.besu.plugins.stream.model.DomainObjectType.BLOCK;
import static net.consensys.besu.plugins.stream.model.DomainObjectType.LOG;
import static net.consensys.besu.plugins.stream.model.DomainObjectType.NODE;
import static net.consensys.besu.plugins.stream.model.DomainObjectType.TRANSACTION;

import net.consensys.besu.plugins.stream.api.config.EventStreamConfiguration;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.api.monitoring.HealthCheck;
import net.consensys.besu.plugins.stream.core.config.EventSchema;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.plugin.services.BesuEvents;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;

/**
 * EventStreamPlugins is a class that provides basic plugin lifecycle operations that are common and
 * agnostic to the message broker implementation.
 *
 * <p>Plugin lifecycle steps:
 *
 * <ul>
 *   <li>register
 *   <li>start
 *   <li>stop
 * </ul>
 *
 * @param <T> the type of the configuration object
 */
public abstract class EventStreamPlugin<T extends EventStreamConfiguration> implements BesuPlugin {

  private static final Logger LOGGER = LogManager.getLogger();

  private final String name;
  private final T configuration;
  private final Function<T, Publisher> publisherFactory;
  private final HealthCheck<T> health;
  private TopicResolver topicResolver;
  private BesuContext context;
  private BesuEventSubscriptionManager subscriptionManager;

  protected EventStreamPlugin(
      final String name,
      final T configuration,
      final Function<T, Publisher> publisherFactory,
      final HealthCheck<T> health) {
    this.name = name;
    this.configuration = configuration;
    this.publisherFactory = publisherFactory;
    this.health = health;
  }

  protected EventStreamPlugin(
      final String name, final T configuration, final Function<T, Publisher> publisherFactory) {
    this(name, configuration, publisherFactory, EventStreamPlugin::noopHealthCheck);
  }

  /**
   * Retrieves the {@link PicoCLIOptions} service to add specific configuration object as a command
   * line object.
   *
   * @param context the {@link BesuContext} to use
   */
  @Override
  public void register(final BesuContext context) {
    LOGGER.debug("Registering plugin for {}", name);
    this.context = context;
    context
        .getService(PicoCLIOptions.class)
        .ifPresent(picoCLIOptions -> picoCLIOptions.addPicoCLIOptions(name, configuration));
    LOGGER.debug("Plugin registered for {}", name);
  }

  /**
   * Checks the health of the underlying message broker, subscribes to different listeners and
   * starts the plugin.
   */
  @Override
  public void start() {
    if (!configuration.isEnabled()) {
      LOGGER.debug("Plugin for {} not enabled", name);
      return;
    }
    LOGGER.debug("Starting plugin for {}", name);
    this.topicResolver = new DomainObjectTopicResolver(configuration::getTopic);
    if (!health.isHealthy(configuration)) {
      LOGGER.error("Connection to the broker is not healthy, aborting plugin start-up.");
      this.stop();
      return;
    }
    LOGGER.debug("Connection to the broker is healthy.");
    if ((!configuration.getLogFilterAddresses().isEmpty()
            || !configuration
                .getLogFilterTopics()
                .isEmpty()) // address or topic filters were specified on command line
        && !configuration
            .getEventSchemas()
            .getSchemas()
            .isEmpty() // contract schema were found in config file (which also has addresses and
    // topics)
    ) {
      LOGGER.info(
          "Ethereum Event filters detected on command line and in configuration file. Taking the union of the two.");
    }

    subscriptionManager =
        new BesuEventSubscriptionManager(
            name,
            BesuEventListener.create(
                context, publisherFactory.apply(configuration), topicResolver, configuration));
    context
        .getService(BesuEvents.class)
        .ifPresent(
            events -> {
              final List<DomainObjectType> enabledTopics = configuration.getEnabledTopics();
              LOGGER.info("Enabled Kafka topics {}", enabledTopics);
              if (enabledTopics.contains(BLOCK)) {
                subscriptionManager
                    .addSubscription(
                        "block propagated",
                        events::addBlockPropagatedListener,
                        events::removeBlockPropagatedListener)
                    .addSubscription(
                        "block added",
                        events::addBlockAddedListener,
                        events::removeBlockAddedListener)
                    .addSubscription(
                        "block reorg",
                        events::addBlockReorgListener,
                        events::removeBlockReorgListener);
              }
              if (enabledTopics.contains(TRANSACTION)) {
                subscriptionManager
                    .addSubscription(
                        "transaction added",
                        events::addTransactionAddedListener,
                        events::removeTransactionAddedListener)
                    .addSubscription(
                        "transaction dropped",
                        events::addTransactionDroppedListener,
                        events::removeTransactionDroppedListener);
              }
              if (enabledTopics.contains(LOG)) {
                subscriptionManager
                    .addSubscription(
                        "log",
                        listener ->
                            events.addLogListener(
                                configuration.getLogFilterAddresses().stream()
                                    .map(besuPluginAddress -> (Address) besuPluginAddress)
                                    .collect(toUnmodifiableList()),
                                configuration.getLogFilterTopics(),
                                listener),
                        events::removeLogListener) // add log listener from command line
                    .addSubscription(
                        "log",
                        listener ->
                            events.addLogListener(
                                configuration.getEventSchemas().getSchemas().stream()
                                    .map(EventSchema::getContractAddress)
                                    .collect(toUnmodifiableList()),
                                List.of(
                                    configuration.getEventSchemas().getSchemas().stream()
                                        .map(EventSchema::getTopic)
                                        .collect(toUnmodifiableList())),
                                listener),
                        events::removeLogListener); // add log listener from config file
              }
              if (enabledTopics.contains(NODE)) {
                subscriptionManager.addSubscription(
                    "sync status", events::addSyncStatusListener, events::removeSyncStatusListener);
              }
              subscriptionManager.subscribeAll();
            });

    configuration.loadEventSchemas();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping plugin for {}", name);
    if (subscriptionManager != null) {
      subscriptionManager.unsubscribeAll();
    }
  }

  @Override
  public CompletableFuture<Void> reloadConfiguration() {
    configuration.loadEventSchemas();
    return CompletableFuture.completedFuture(null);
  }

  @VisibleForTesting
  public T getConfiguration() {
    return configuration;
  }

  @VisibleForTesting
  public TopicResolver getTopicResolver() {
    return topicResolver;
  }

  @VisibleForTesting
  public static <T> boolean noopHealthCheck(final T configuration) {
    return true;
  }
}
