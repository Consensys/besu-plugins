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

import net.consensys.besu.plugins.stream.api.config.CommonConfiguration;
import net.consensys.besu.plugins.stream.core.config.AddressTypeConverter;
import net.consensys.besu.plugins.stream.core.config.LogFilterTopicsWrapper;
import net.consensys.besu.plugins.stream.core.config.TopicTypeConverter;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.List;

import picocli.CommandLine.Option;

public class RabbitMqPluginConfiguration extends CommonConfiguration {

  @Option(names = "--plugin-rabbitmq-host", hidden = true)
  private String host = "localhost";

  @Option(names = "--plugin-rabbitmq-port", hidden = true)
  private Integer port = 5672;

  @Option(names = "--plugin-rabbitmq-username", hidden = true)
  private String username;

  @Option(names = "--plugin-rabbitmq-password", hidden = true)
  private String password;

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public void setPort(final Integer port) {
    this.port = port;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Option(names = "--plugin-rabbitmq-enabled", description = "Enables event streaming plugin.")
  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
  }

  @Option(
      names = "--plugin-rabbitmq-stream",
      description = "Event stream/topic to send events to (default: ${DEFAULT-VALUE})")
  @Override
  public void setTopic(final String topic) {
    super.setTopic(topic);
  }

  @Option(
      names = "--plugin-rabbitmq-url",
      description = "URL of broker server (default: ${DEFAULT-VALUE})")
  @Override
  public void setBrokerUrl(final String brokerUrl) {
    super.setBrokerUrl(brokerUrl);
  }

  @Option(
      names = "--plugin-rabbitmq-metadata-db-enabled",
      description =
          "Enable to store events metadata in a local database (default: ${DEFAULT-VALUE})",
      arity = "1")
  @Override
  public void setMetadataDBEnabled(final boolean metadataDBEnabled) {
    super.setMetadataDBEnabled(metadataDBEnabled);
  }

  @Option(names = "--plugin-rabbitmq-log-filter-topics", converter = TopicTypeConverter.class)
  @Override
  public void setLogFilterTopicsWrapper(final LogFilterTopicsWrapper logFilterTopicsWrapper) {
    super.setLogFilterTopicsWrapper(logFilterTopicsWrapper);
  }

  @Option(names = "--plugin-rabbitmq-log-filter-addresses", converter = AddressTypeConverter.class)
  @Override
  public void setLogFilterAddresses(final List<Address> logFilterAddresses) {
    super.setLogFilterAddresses(logFilterAddresses);
  }

  @Option(names = "--plugin-rabbitmq-log-schema-file")
  @Override
  public void setEventSchemasFile(final File eventSchemasFile) {
    super.setEventSchemasFile(eventSchemasFile);
  }
}
