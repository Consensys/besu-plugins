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
package net.consensys.besu.plugins.evenstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import net.consensys.besu.plugin.kinesis.KinesisPluginConfiguration;
import net.consensys.besu.plugins.dsl.cli.CommandLineTest;
import net.consensys.besu.plugins.types.Address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class KinesisPluginCommandLineTest {
  private KinesisPluginConfiguration options;
  private final String brokerURL = "99.88.77.66:9092";
  private final String topic = "topic.test";
  private final String logFilterTopics =
      "[[\"0xd3610b1c54575b7f4f0dc03d210b8ac55624ae007679b7a928a4f25a709331a8\"],[\"0x0000000000000000000000000000000000000000000000000000000000000005\",\"0x0000000000000000000000000000000000000000000000000000000000000009\"]]";
  private final String address = "0xcb4f3cA3777fE16FBF4595Ba48d0eBFAEfEaBEBc";
  private final String logFilterAddresses = String.format("%s", address);
  private final String logSchemaFile = "/tmp/schema";
  private final String keyID = "keyID";
  private final String keySecret = "keySecret";
  private final String region = "us-east-2";

  @BeforeEach
  public void setUp() {
    options = new KinesisPluginConfiguration();
  }

  @Test
  public void givenNormalConditions_assertThatParseWork() {
    new CommandLineTest(
        "kinesis",
        options,
        "--plugin-kinesis-endpoint-override",
        brokerURL,
        "--plugin-kinesis-enabled",
        "--plugin-kinesis-stream",
        topic,
        "--plugin-kinesis-metadata-db-enabled=true",
        "--plugin-kinesis-log-filter-topics",
        logFilterTopics,
        "--plugin-kinesis-log-filter-addresses",
        logFilterAddresses,
        "--plugin-kinesis-log-schema-file",
        logSchemaFile,
        "--plugin-kinesis-aws-access-key-id",
        keyID,
        "--plugin-kinesis-aws-secret-key",
        keySecret,
        "--plugin-kinesis-aws-region",
        region);
    assertThat(options.isEnabled()).isTrue();
    assertThat(options.getTopic()).isEqualTo(topic);
    assertThat(options.getTopic()).isEqualTo(topic);
    assertThat(options.isMetadataDBEnabled()).isTrue();
    assertThat(options.getLogFilterTopics()).hasSize(2);
    assertThat(options.getLogFilterAddresses())
        .hasSize(1)
        .containsExactly(Address.fromHexString(address));
    assertThat(options.getEventSchemasFile())
        .matches(file -> file.getAbsolutePath().equals(logSchemaFile));
    assertThat(options.getAwsAccessKeyId()).isEqualTo(keyID);
    assertThat(options.getAwsSecretKey()).isEqualTo(keySecret);
    assertThat(options.getRegion()).isEqualTo(region);
  }

  @Test
  public void givenMissingRequiredParameter_assertThatParseFail() {
    assertThatThrownBy(
            () -> new CommandLineTest("kinesis", options, "--plugin-kinesis-endpoint-override"))
        .isInstanceOf(CommandLine.MissingParameterException.class)
        .hasMessageContaining("Missing required parameter");
  }

  @Test
  public void givenUnknownOption_assertThatParseFail() {
    assertThatThrownBy(() -> new CommandLineTest("kinesis", options, "--plugin-kinesis-endpoint"))
        .isInstanceOf(CommandLine.UnmatchedArgumentException.class)
        .hasMessageContaining("Unknown option");
  }

  @Test
  public void givenArityNotRespected_assertThatParseFail() {
    assertThatThrownBy(
            () ->
                new CommandLineTest(
                    "kinesis",
                    options,
                    "--plugin-kinesis-endpoint-override",
                    brokerURL,
                    "--plugin-kinesis-endpoint-override",
                    brokerURL))
        .isInstanceOf(CommandLine.OverwrittenOptionException.class)
        .hasMessageContaining("should be specified only once");
  }
}
