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

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.Publisher;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;

/**
 * Utility class used to publish event objects to kinesis stream.
 *
 * @since 0.1
 */
public class KinesisPublisher implements Publisher {
  private static final Logger LOGGER = LogManager.getLogger(KinesisPublisher.class);
  private final KinesisClient client;

  public KinesisPublisher(final KinesisClient client) {
    this.client = client;
  }

  /**
   * Build a {@link KinesisPublisher} instance from specified configuration.
   *
   * @param pluginConfiguration The configuration to apply.
   * @return An instance of {@link KinesisPublisher}.
   */
  static KinesisPublisher build(final KinesisPluginConfiguration pluginConfiguration) {
    final KinesisPublisher publisher =
        new KinesisPublisher(
            KinesisClientFactory.create(
                pluginConfiguration::getAwsAccessKeyId,
                pluginConfiguration::getAwsSecretKey,
                pluginConfiguration::getRegion,
                pluginConfiguration::getEndpointOverride));
    publisher.createStreamAsync(pluginConfiguration.getTopic(), 1);
    return publisher;
  }

  private void createStreamAsync(final String streamName, final int shards) {
    Executors.defaultThreadFactory().newThread(() -> createStream(streamName, shards)).start();
  }

  private void createStream(final String streamName, final int shards) {
    try {
      client.describeStream(DescribeStreamRequest.builder().streamName(streamName).build());
    } catch (final ResourceNotFoundException e) {
      LOGGER.debug("Stream not found.");
      LOGGER.debug("Creating kinesis stream: {} with {} shards", streamName, shards);
      final CreateStreamRequest createStreamRequest =
          CreateStreamRequest.builder().streamName(streamName).shardCount(shards).build();
      final CreateStreamResponse createStreamResponse = client.createStream(createStreamRequest);
      LOGGER.debug("Create stream result: {}", createStreamResponse.toString());
      return;
    }
    LOGGER.debug("Stream already exists.");
  }

  /**
   * Publish an event to a kinesis stream.
   *
   * @param event The event to publish.
   */
  @Override
  public void publish(
      final DomainObjectType domainObjectType,
      final TopicResolver topicResolver,
      final Event event) {
    final String topic = topicResolver.resolve(domainObjectType, event);
    LOGGER.debug("Publishing in stream: {}", topic);
    // Add record to the kinesis producer.
    final PutRecordResponse putRecordResponse =
        client.putRecord(
            PutRecordRequest.builder()
                .data(SdkBytes.fromByteBuffer(event.buffer()))
                .streamName(topic)
                .partitionKey(KinesisPluginConfiguration.DEFAULT_PARTITION_KEY)
                .build());
    LOGGER.debug("Successfully published with shardID: {}", putRecordResponse.shardId());
  }
}
