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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import net.consensys.besu.plugins.stream.api.config.EventStreamConfiguration;
import net.consensys.besu.plugins.stream.api.event.Publisher;

import java.util.function.Function;

import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.services.BesuEvents;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventStreamPluginTest {

  @Mock private EventStreamConfiguration configuration;
  @Mock private Publisher publisher;
  @Mock private BesuContext context;

  @Test
  void assertThatRegisterGetsPicoCLIOptionsService() {
    final EventStreamPlugin<EventStreamConfiguration> plugin =
        new Plugin<>("test-plugin", configuration, ignored -> publisher);
    plugin.register(context);
    verify(context).getService(PicoCLIOptions.class);
  }

  @Test
  void assertStartDoesNothingIfPluginNotEnabled() {
    final EventStreamPlugin<EventStreamConfiguration> plugin =
        new Plugin<>("test-plugin", configuration, ignored -> publisher);
    plugin.register(context);
    verify(context).getService(PicoCLIOptions.class);
    plugin.start();
    verifyNoMoreInteractions(context);
  }

  @Test
  void assertStartWorksIfPluginEnabled() {
    final EventStreamPlugin<EventStreamConfiguration> plugin =
        new Plugin<>("test-plugin", configuration, ignored -> publisher);
    when(configuration.isEnabled()).thenReturn(true);
    when(configuration.getTopic()).thenReturn("test-topic");
    plugin.register(context);
    verify(context).getService(PicoCLIOptions.class);
    plugin.start();
    verify(context).getService(BesuEvents.class);
    assertThat(plugin.getTopicResolver()).isNotNull().isInstanceOf(DomainObjectTopicResolver.class);
  }

  @Test
  void assertThatConfigurationReloadWorks() {
    final EventStreamPlugin<EventStreamConfiguration> plugin =
        new Plugin<>("test-plugin", configuration, ignored -> publisher);
    plugin.reloadConfiguration();
    verify(configuration).loadEventSchemas();
  }

  static class Plugin<T extends EventStreamConfiguration> extends EventStreamPlugin<T> {

    Plugin(
        final String name, final T configuration, final Function<T, Publisher> publisherFactory) {
      super(name, configuration, publisherFactory);
    }
  }
}
