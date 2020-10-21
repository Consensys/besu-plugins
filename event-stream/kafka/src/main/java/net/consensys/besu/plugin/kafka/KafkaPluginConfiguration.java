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

import net.consensys.besu.plugins.stream.api.config.CommonConfiguration;
import net.consensys.besu.plugins.stream.core.config.AddressTypeConverter;
import net.consensys.besu.plugins.stream.core.config.DomainObjectTypeConverter;
import net.consensys.besu.plugins.stream.core.config.LogFilterTopicsWrapper;
import net.consensys.besu.plugins.stream.core.config.TopicTypeConverter;
import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.annotations.VisibleForTesting;
import org.apache.kafka.clients.producer.ProducerConfig;
import picocli.CommandLine.Option;

/**
 * Wrapper class for all required configuration properties.
 *
 * @since 0.1
 */
public final class KafkaPluginConfiguration extends CommonConfiguration {

  @VisibleForTesting static final String SASL_CONFIG_PROPERTY_KEY = "sasl.jaas.config";

  @Option(
      names = "--plugin-kafka-producer-property",
      description = "Additional Kafka producer properties.")
  private Map<String, String> producerProperties;

  @Option(
      names = "--plugin-kafka-producer-config-override-enabled",
      description = "Enables overriding of Kafka producer properties.")
  private boolean producerConfigOverrideEnabled = false;

  public Properties properties() throws ClassNotFoundException {
    return saslConfig(
        KafkaHelper.extendWith(
            defaultProperties(), producerProperties, producerConfigOverrideEnabled));
  }

  private Properties saslConfig(final Properties properties) {
    if (properties.containsKey(SASL_CONFIG_PROPERTY_KEY)) {
      properties.put(
          SASL_CONFIG_PROPERTY_KEY,
          properties
              .get(SASL_CONFIG_PROPERTY_KEY)
              .toString()
              .replaceAll("username=([a-zA-Z0-9_+/]+)", "username=\"$1\"")
              .replaceAll("password=([a-zA-Z0-9_+/]+)", "password=\"$1\""));
    }
    return properties;
  }

  private Properties defaultProperties() throws ClassNotFoundException {
    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBrokerUrl());
    props.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
    props.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
    return props;
  }

  public Map<String, String> getProducerProperties() {
    return producerProperties;
  }

  public boolean isProducerConfigOverrideEnabled() {
    return producerConfigOverrideEnabled;
  }

  public void setProducerConfigOverrideEnabled(final boolean producerConfigOverrideEnabled) {
    this.producerConfigOverrideEnabled = producerConfigOverrideEnabled;
  }

  public void setProducerProperties(final Map<String, String> producerProperties) {
    this.producerProperties = producerProperties;
  }

  @Option(names = "--plugin-kafka-enabled", description = "Enables event streaming plugin.")
  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
  }

  @Option(
      names = "--plugin-kafka-stream",
      description = "Event stream/topic to send events to (default: ${DEFAULT-VALUE})")
  @Override
  public void setTopic(final String topic) {
    super.setTopic(topic);
  }

  @Option(
      names = "--plugin-kafka-url",
      description = "URL of broker server (default: ${DEFAULT-VALUE})")
  @Override
  public void setBrokerUrl(final String brokerUrl) {
    super.setBrokerUrl(brokerUrl);
  }

  @Option(
      names = "--plugin-kafka-metadata-db-enabled",
      description =
          "Enable to store events metadata in a local database (default: ${DEFAULT-VALUE})",
      arity = "1")
  @Override
  public void setMetadataDBEnabled(final boolean metadataDBEnabled) {
    super.setMetadataDBEnabled(metadataDBEnabled);
  }

  @Option(
      names = {"--plugin-kafka-enabled-topic", "--plugin-kafka-enabled-topics"},
      paramLabel = "<topic name>",
      split = ",",
      arity = "1..*",
      description = "Comma separated list of topics to enable",
      converter = DomainObjectTypeConverter.class)
  @Override
  public void setEnabledTopics(final List<DomainObjectType> enabledTopics) {
    super.setEnabledTopics(enabledTopics);
  }

  @Option(names = "--plugin-kafka-log-filter-topics", converter = TopicTypeConverter.class)
  @Override
  public void setLogFilterTopicsWrapper(final LogFilterTopicsWrapper logFilterTopicsWrapper) {
    super.setLogFilterTopicsWrapper(logFilterTopicsWrapper);
  }

  @Option(
      names = "--plugin-kafka-log-filter-addresses",
      paramLabel = "<address>",
      split = ",",
      arity = "1..*",
      description = "Comma separated list of addresses",
      converter = AddressTypeConverter.class)
  @Override
  public void setLogFilterAddresses(final List<Address> logFilterAddresses) {
    super.setLogFilterAddresses(logFilterAddresses);
  }

  @Option(names = "--plugin-kafka-log-schema-file")
  @Override
  public void setEventSchemasFile(final File eventSchemasFile) {
    super.setEventSchemasFile(eventSchemasFile);
  }
}
