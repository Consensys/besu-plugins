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

import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class KafkaPluginConfigurationTest {

  @Test
  void assertThatSchemaFileReloads() {
    final KafkaPluginConfiguration configuration = new KafkaPluginConfiguration();
    assertThat(configuration.getEventSchemas().getSchemas()).isEmpty();
    configuration.setEventSchemasFile(
        new File(this.getClass().getResource("/example-event-schema-config.yaml").getFile()));
    configuration.loadEventSchemas();
    assertThat(configuration.getEventSchemas().getSchemas()).isNotEmpty();
  }

  @Test
  void assertThatDoubleQuotesAreAdded() throws Exception {
    final Map<String, String> additionalProperties = new HashMap<>();
    additionalProperties.put(
        KafkaPluginConfiguration.SASL_CONFIG_PROPERTY_KEY,
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=DUMMY_USERNAME password=DUMMY_PASSWORD;");
    final KafkaPluginConfiguration configuration = new KafkaPluginConfiguration();
    configuration.setProducerProperties(additionalProperties);
    assertThat(configuration.properties().get(KafkaPluginConfiguration.SASL_CONFIG_PROPERTY_KEY))
        .isEqualTo(
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"DUMMY_USERNAME\" password=\"DUMMY_PASSWORD\";");
  }

  @Test
  public void pluginKafkaEnabledTopicIsParsedCorrectly() {
    final KafkaPluginConfiguration kafkaPluginConfiguration = new KafkaPluginConfiguration();
    final CommandLine commandLine = new CommandLine(kafkaPluginConfiguration);

    commandLine.parseArgs("--plugin-kafka-enabled-topic", "BLOCK");
    assertThat(kafkaPluginConfiguration.getEnabledTopics()).containsExactly(DomainObjectType.BLOCK);

    commandLine.parseArgs("--plugin-kafka-enabled-topic", "block");
    assertThat(kafkaPluginConfiguration.getEnabledTopics()).containsExactly(DomainObjectType.BLOCK);

    commandLine.parseArgs("--plugin-kafka-enabled-topics", "block,log");
    assertThat(kafkaPluginConfiguration.getEnabledTopics())
        .containsExactlyInAnyOrder(DomainObjectType.LOG, DomainObjectType.BLOCK);
  }

  @Test
  public void pluginKafkaLogFilterAddressesIsParsedCorrectly() {
    final KafkaPluginConfiguration kafkaPluginConfiguration = new KafkaPluginConfiguration();
    final CommandLine commandLine = new CommandLine(kafkaPluginConfiguration);

    List<Address> addresses =
        Arrays.asList(
            Address.fromHexString("0xF216B6b2D9E76F94f97bE597e2Cec81730520585"),
            Address.fromHexString("0xDDDDB6b2D9E76F94f97bE597e2Cec81730520572"));

    commandLine.parseArgs("--plugin-kafka-log-filter-addresses", addresses.get(0).toHexString());
    assertThat(kafkaPluginConfiguration.getLogFilterAddresses()).containsExactly(addresses.get(0));

    commandLine.parseArgs(
        "--plugin-kafka-log-filter-addresses",
        addresses.get(0).toHexString(),
        addresses.get(1).toHexString());
    assertThat(kafkaPluginConfiguration.getLogFilterAddresses())
        .containsExactlyInAnyOrderElementsOf(addresses);
  }

  @Test
  public void allTopicsShouldBeEnabledByDefault() {
    final KafkaPluginConfiguration kafkaPluginConfiguration = new KafkaPluginConfiguration();
    final CommandLine commandLine = new CommandLine(kafkaPluginConfiguration);

    assertThat(kafkaPluginConfiguration.getEnabledTopics())
        .containsExactly(DomainObjectType.values());

    commandLine.parseArgs();
    assertThat(kafkaPluginConfiguration.getEnabledTopics())
        .containsExactly(DomainObjectType.values());
  }
}
