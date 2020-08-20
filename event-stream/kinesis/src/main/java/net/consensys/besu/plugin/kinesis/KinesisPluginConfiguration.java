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
package net.consensys.besu.plugin.kinesis;

import net.consensys.besu.plugins.stream.api.config.CommonConfiguration;
import net.consensys.besu.plugins.stream.core.config.AddressTypeConverter;
import net.consensys.besu.plugins.stream.core.config.LogFilterTopicsWrapper;
import net.consensys.besu.plugins.stream.core.config.TopicTypeConverter;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.List;

import picocli.CommandLine.Option;

/**
 * Wrapper class for all required configuration properties.
 *
 * @since 0.1
 */
public final class KinesisPluginConfiguration extends CommonConfiguration {
  public static final String DEFAULT_PARTITION_KEY = "0";

  @Option(
      names = "--plugin-kinesis-aws-region",
      description = "AWS region of Kinesis stream (default: ${DEFAULT-VALUE})")
  private String region = "us-east-2";

  @Option(names = "--plugin-kinesis-aws-access-key-id", description = "AWS access key id")
  private String awsAccessKeyId;

  @Option(names = "--plugin-kinesis-aws-secret-key", description = "AWS secret access key")
  private String awsSecretKey;

  @Option(
      names = "--plugin-kinesis-endpoint-override",
      description = "Explicitly configured Kinesis endpoint",
      hidden = true)
  private String endpointOverride;

  public String getRegion() {
    return region;
  }

  public String getAwsAccessKeyId() {
    return awsAccessKeyId;
  }

  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  public String getEndpointOverride() {
    return endpointOverride;
  }

  @Option(names = "--plugin-kinesis-enabled", description = "Enables event streaming plugin.")
  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
  }

  @Option(
      names = "--plugin-kinesis-stream",
      description = "Event stream/topic to send events to (default: ${DEFAULT-VALUE})")
  @Override
  public void setTopic(final String topic) {
    super.setTopic(topic);
  }

  @Option(
      names = "--plugin-kinesis-url",
      description = "URL of broker server (default: ${DEFAULT-VALUE})")
  @Override
  public void setBrokerUrl(final String brokerUrl) {
    super.setBrokerUrl(brokerUrl);
  }

  @Option(
      names = "--plugin-kinesis-metadata-db-enabled",
      description =
          "Enable to store events metadata in a local database (default: ${DEFAULT-VALUE})",
      arity = "1")
  @Override
  public void setMetadataDBEnabled(final boolean metadataDBEnabled) {
    super.setMetadataDBEnabled(metadataDBEnabled);
  }

  @Option(names = "--plugin-kinesis-log-filter-topics", converter = TopicTypeConverter.class)
  @Override
  public void setLogFilterTopicsWrapper(final LogFilterTopicsWrapper logFilterTopicsWrapper) {
    super.setLogFilterTopicsWrapper(logFilterTopicsWrapper);
  }

  @Option(names = "--plugin-kinesis-log-filter-addresses", converter = AddressTypeConverter.class)
  @Override
  public void setLogFilterAddresses(final List<Address> logFilterAddresses) {
    super.setLogFilterAddresses(logFilterAddresses);
  }

  @Option(names = "--plugin-kinesis-log-schema-file")
  @Override
  public void setEventSchemasFile(final File eventSchemasFile) {
    super.setEventSchemasFile(eventSchemasFile);
  }
}
