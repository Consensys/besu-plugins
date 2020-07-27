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
package net.consensys.besu.plugin.rabbitmq;

import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.monitoring.HealthCheck;
import net.consensys.besu.plugins.stream.core.EventStreamPlugin;

import java.util.function.Function;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import org.hyperledger.besu.plugin.BesuPlugin;

/**
 * Besu plugin entrypoint
 *
 * @see BesuPlugin
 * @since 0.1
 */
@AutoService(BesuPlugin.class)
public class RabbitMqPlugin extends EventStreamPlugin<RabbitMqPluginConfiguration>
    implements BesuPlugin {

  @VisibleForTesting
  RabbitMqPlugin(
      final String name,
      final RabbitMqPluginConfiguration configuration,
      final Function<RabbitMqPluginConfiguration, Publisher> publisherFactory,
      final HealthCheck<RabbitMqPluginConfiguration> health) {
    super(name, configuration, publisherFactory, health);
  }
}
