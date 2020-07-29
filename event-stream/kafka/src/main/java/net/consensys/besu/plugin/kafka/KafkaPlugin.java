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

import net.consensys.besu.plugin.kafka.health.KafkaHealthChecker;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.monitoring.HealthCheck;
import net.consensys.besu.plugins.stream.core.EventStreamPlugin;

import java.util.function.Function;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import org.hyperledger.besu.plugin.BesuPlugin;

/**
 * Kafka Besu plugin entrypoint. This defines the Kafka specific implementations required to be used
 * as an event streaming plugin.
 *
 * @see BesuPlugin
 */
@AutoService(BesuPlugin.class)
public class KafkaPlugin extends EventStreamPlugin<KafkaPluginConfiguration> implements BesuPlugin {

  public KafkaPlugin() {
    super(
        "kafka",
        new KafkaPluginConfiguration(),
        KafkaPublisher::build,
        KafkaHealthChecker::isHealthy);
  }

  @VisibleForTesting
  KafkaPlugin(
      final String name,
      final KafkaPluginConfiguration configuration,
      final Function<KafkaPluginConfiguration, Publisher> publisherFactory,
      final HealthCheck<KafkaPluginConfiguration> health) {
    super(name, configuration, publisherFactory, health);
  }
}
