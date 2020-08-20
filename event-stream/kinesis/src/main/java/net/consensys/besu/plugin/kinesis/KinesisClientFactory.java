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

import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

class KinesisClientFactory {
  private static final Logger LOGGER = LogManager.getLogger(KinesisClientFactory.class);

  static KinesisClient create(
      final Supplier<String> awsAccessKeyIdSupplier,
      final Supplier<String> awsSecretKeySupplier,
      final Supplier<String> regionSupplier,
      final Supplier<String> endpointOverrideSupplier) {
    LOGGER.debug("Creating KinesisClient.");
    KinesisClientBuilder clientBuilder = KinesisClient.builder();
    return clientBuilder(
            CredentialsProvider::of,
            clientBuilder::credentialsProvider,
            awsAccessKeyIdSupplier,
            awsSecretKeySupplier,
            regionSupplier,
            clientBuilder::region,
            endpointOverrideSupplier,
            clientBuilder::endpointOverride)
        .build();
  }

  static KinesisAsyncClient createAsyncClient(
      final Supplier<String> awsAccessKeyIdSupplier,
      final Supplier<String> awsSecretKeySupplier,
      final Supplier<String> regionSupplier,
      final Supplier<String> endpointOverrideSupplier) {
    LOGGER.debug("Creating KinesisAsyncClient.");
    KinesisAsyncClientBuilder clientBuilder = KinesisAsyncClient.builder();
    return clientBuilder(
            CredentialsProvider::of,
            clientBuilder::credentialsProvider,
            awsAccessKeyIdSupplier,
            awsSecretKeySupplier,
            regionSupplier,
            clientBuilder::region,
            endpointOverrideSupplier,
            clientBuilder::endpointOverride)
        .build();
  }

  private static <T> T clientBuilder(
      final CredentialsProviderFunction credentialsProviderFunction,
      final Function<AwsCredentialsProvider, T> credentialsProviderConsumer,
      final Supplier<String> awsAccessKeyIdSupplier,
      final Supplier<String> awsSecretKeySupplier,
      final Supplier<String> regionSupplier,
      final Function<Region, T> regionConsumer,
      final Supplier<String> endpointOverrideSupplier,
      final Function<URI, T> endpointOverrideFunction) {
    T clientBuilder =
        credentialsProviderConsumer.apply(
            credentialsProviderFunction.provider(awsAccessKeyIdSupplier, awsSecretKeySupplier));
    if (regionSupplier.get() != null && !regionSupplier.get().isBlank()) {
      clientBuilder = regionConsumer.apply(Region.of(regionSupplier.get()));
    }
    if (endpointOverrideSupplier.get() != null && !endpointOverrideSupplier.get().isBlank()) {
      System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
      clientBuilder = endpointOverrideFunction.apply(URI.create(endpointOverrideSupplier.get()));
    }
    return clientBuilder;
  }

  @FunctionalInterface
  interface CredentialsProviderFunction {
    AwsCredentialsProvider provider(
        final Supplier<String> awsAccessKeyIdSupplier, final Supplier<String> awsSecretKeySupplier);
  }
}
