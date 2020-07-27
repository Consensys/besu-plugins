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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;

class KinesisHealthChecker {
  private static final Logger LOGGER = LogManager.getLogger(KinesisHealthChecker.class);

  static boolean isHealthy(final KinesisPluginConfiguration configuration) {
    try {
      LOGGER.info("Checking for readiness.");
      LOGGER.debug("Creating Kinesis client with provided configuration.");
      final KinesisClient kinesisClient =
          KinesisClientFactory.create(
              configuration::getAwsAccessKeyId,
              configuration::getAwsSecretKey,
              configuration::getRegion,
              configuration::getEndpointOverride);
      // Try to list stream to check if connection is healthy
      return kinesisClient.listStreams(ListStreamsRequest.builder().limit(1).build()) != null;
    } catch (final SdkClientException | AwsServiceException e) {
      LOGGER.warn(e);
      return false;
    }
  }
}
