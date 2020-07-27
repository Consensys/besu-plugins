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

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaHelper {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Extends the passed properties by adding missing additional properties specified as second
   * parameter.
   *
   * @param properties The initial properties.
   * @param additionalProperties The additional properties to add if not present.
   * @param overrideEnabled true if override is enabled, false otherwise.
   * @return The modified set of properties.
   */
  public static Properties extendWith(
      final Properties properties,
      final Map<String, String> additionalProperties,
      final boolean overrideEnabled) {
    if (additionalProperties != null) {
      additionalProperties.forEach(
          (propertyKey, propertyValue) -> {
            if (!overrideEnabled && properties.containsKey(propertyKey)) {
              LOGGER.info(
                  "Cannot override property {} because override is not enabled.", propertyKey);
            } else {
              LOGGER.info("Adding or updating property {}.", propertyKey);
              properties.put(propertyKey, propertyValue);
            }
          });
    }
    return properties;
  }
}
