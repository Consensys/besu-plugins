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
package net.consensys.besu.plugin.kafka.health;

import net.consensys.besu.plugin.kafka.KafkaPluginConfiguration;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaHealthChecker {
  private static final Logger LOGGER = LogManager.getLogger(KafkaHealthChecker.class);

  public static boolean isHealthy(final KafkaPluginConfiguration configuration) {
    LOGGER.info("Checking for readiness of bootstrap servers.");
    Thread.currentThread().setContextClassLoader(KafkaHealthChecker.class.getClassLoader());
    try (AdminClient client = KafkaAdminClient.create(configuration.properties())) {
      final ListTopicsResult topics = client.listTopics();
      final Set<String> names = topics.names().get();
      if (names.isEmpty()) {
        LOGGER.debug("No topic found.");
      }
      LOGGER.info("Bootstrap servers are ready.");
      return true;
    } catch (InterruptedException | ExecutionException | ClassNotFoundException e) {
      LOGGER.warn(e);
      return false;
    } catch (KafkaException e) {
      LOGGER.error("KafkaException caught.", e);
      return false;
    }
  }
}
