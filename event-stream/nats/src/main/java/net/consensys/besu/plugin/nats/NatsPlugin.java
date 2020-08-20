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
public class NatsPlugin
    extends EventStreamPlugin<net.consensys.besu.plugin.nats.NatsPluginConfiguration>
    implements BesuPlugin {
  public NatsPlugin() {
    super(
        "nats",
        new net.consensys.besu.plugin.nats.NatsPluginConfiguration(),
        net.consensys.besu.plugin.nats.NatsPublisher::build);
  }

  @VisibleForTesting
  NatsPlugin(
      final String name,
      final net.consensys.besu.plugin.nats.NatsPluginConfiguration configuration,
      final Function<net.consensys.besu.plugin.nats.NatsPluginConfiguration, Publisher>
          publisherFactory,
      final HealthCheck<net.consensys.besu.plugin.nats.NatsPluginConfiguration> health) {
    super(name, configuration, publisherFactory, health);
  }
}
