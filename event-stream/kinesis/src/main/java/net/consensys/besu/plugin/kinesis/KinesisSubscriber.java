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

import net.consensys.besu.plugins.stream.api.event.EventHandler;
import net.consensys.besu.plugins.stream.api.event.Subscriber;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.ConsumerStatus;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamConsumerRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamConsumerResponse;
import software.amazon.awssdk.services.kinesis.model.RegisterStreamConsumerRequest;
import software.amazon.awssdk.services.kinesis.model.RegisterStreamConsumerResponse;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardEvent;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardResponseHandler;

public class KinesisSubscriber implements Subscriber {
  private static final Logger LOGGER = LogManager.getLogger(KinesisSubscriber.class);
  private final KinesisAsyncClient client;
  private final String consumerStreamARN;
  private final String consumerName;

  public static KinesisSubscriber create(
      final Supplier<String> awsAccessKeyIdSupplier,
      final Supplier<String> awsSecretKeySupplier,
      final Supplier<String> regionSupplier,
      final Supplier<String> endpointOverrideSupplier,
      final Supplier<String> consumerStreamARNSupplier,
      final Supplier<String> consumerNameSupplier) {
    LOGGER.debug("Creating subscriber for stream: {}", consumerStreamARNSupplier.get());
    return new KinesisSubscriber(
        KinesisClientFactory.createAsyncClient(
            awsAccessKeyIdSupplier, awsSecretKeySupplier, regionSupplier, endpointOverrideSupplier),
        consumerStreamARNSupplier.get(),
        consumerNameSupplier.get());
  }

  KinesisSubscriber(
      final KinesisAsyncClient client, final String consumerStreamARN, final String consumerName) {
    this.client = client;
    this.consumerStreamARN = consumerStreamARN;
    this.consumerName = consumerName;
  }

  @Override
  public void subscribe(final String topic, final EventHandler eventHandler) {
    try {
      final AtomicReference<String> consumerArn = new AtomicReference<>();
      LOGGER.info("Subscribing to topic: {}", topic);
      // Check if consumer exist, if yes re-use consumer ARN, register consumer otherwise
      consumerExists(consumerName, consumerStreamARN)
          .ifPresentOrElse(consumerArn::set, () -> consumerArn.set(registerConsumer()));
      subscribeToShard(consumerArn.get(), eventHandler);
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Cannot subscribe to topic.", e);
    }
  }

  private String registerConsumer() {
    try {
      LOGGER.info("Consumer does not exist, registering it.");
      final RegisterStreamConsumerRequest registerStreamConsumerRequest =
          RegisterStreamConsumerRequest.builder()
              .streamARN(consumerStreamARN)
              .consumerName(consumerName)
              .build();
      final RegisterStreamConsumerResponse registerStreamConsumerResponse =
          client.registerStreamConsumer(registerStreamConsumerRequest).get();
      LOGGER.info("Waiting for consumer to be active.");
      Thread.sleep(5000);
      return registerStreamConsumerResponse.consumer().consumerARN();
    } catch (Exception e) {
      LOGGER.error("Cannot register consumer.", e);
      throw new RuntimeException("Cannot register consumer", e);
    }
  }

  /**
   * Returns an Optional wrapping the consumer ARN if consumer is ACTIVE, empty Optional otherwise.
   *
   * @param consumerName
   * @param consumerStreamARN
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   */
  private Optional<String> consumerExists(
      final String consumerName, final String consumerStreamARN) {
    final DescribeStreamConsumerRequest describeStreamConsumerRequest =
        DescribeStreamConsumerRequest.builder()
            .streamARN(consumerStreamARN)
            .consumerName(consumerName)
            .build();
    try {
      final DescribeStreamConsumerResponse describeStreamConsumerResponse =
          client.describeStreamConsumer(describeStreamConsumerRequest).get();
      return ConsumerStatus.ACTIVE.equals(
              describeStreamConsumerResponse.consumerDescription().consumerStatus())
          ? Optional.of(describeStreamConsumerResponse.consumerDescription().consumerARN())
          : Optional.empty();
    } catch (final Throwable e) {
      LOGGER.warn("Stream not found.");
      return Optional.empty();
    }
  }

  private void subscribeToShard(final String consumerArn, final EventHandler eventHandler)
      throws ExecutionException, InterruptedException {
    LOGGER.info("Subscribing to shard");
    client
        .subscribeToShard(
            subscribeToShardRequest(consumerArn, "shardId-000000000000"),
            subscribeToShardResponseHandler(eventHandler))
        .get();
  }

  private SubscribeToShardResponseHandler.Visitor toVisitor(final EventHandler eventHandler) {
    return new SubscribeToShardResponseHandler.Visitor() {
      @Override
      public void visit(SubscribeToShardEvent event) {
        event.records().stream()
            .map(record -> record.data().asString(StandardCharsets.UTF_8))
            .forEach(eventHandler::apply);
      }
    };
  }

  private SubscribeToShardResponseHandler subscribeToShardResponseHandler(
      final EventHandler eventHandler) {
    return SubscribeToShardResponseHandler.builder()
        .onError(t -> LOGGER.warn("Error during stream: {}.", t.getMessage()))
        .onComplete(() -> LOGGER.debug("All records stream successfully"))
        .subscriber(toVisitor(eventHandler))
        .build();
  }

  private SubscribeToShardRequest subscribeToShardRequest(
      final String consumerArn, final String shardId) {
    return SubscribeToShardRequest.builder()
        .consumerARN(consumerArn)
        .shardId(shardId)
        .startingPosition(s -> s.type(ShardIteratorType.LATEST))
        .build();
  }
}
