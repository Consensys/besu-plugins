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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class KafkaHelperTest {
  @Test
  void extendWithShouldAddMissingProperties() {
    final int numberOfInitialProperties = 10;
    final Properties properties =
        IntStream.range(0, numberOfInitialProperties)
            .boxed()
            .collect(
                Collectors.toMap(
                    i -> String.format("property-%d", i),
                    i -> String.format("value-%d", i),
                    (a, b) -> b,
                    Properties::new));
    final Map<String, String> additionalProperties = new HashMap<>();
    additionalProperties.put("missing-property-1", "missing-value-1");
    additionalProperties.put(
        properties.keySet().iterator().next().toString(), "overridden-value-1");

    final Properties extendedProperties =
        KafkaHelper.extendWith(properties, additionalProperties, false);
    assertThat(extendedProperties)
        .containsAllEntriesOf(properties)
        .doesNotContainEntry("property-1", "overridden-value-1");
  }

  @Test
  void extendWithShouldAddAllPropertiesIfOverrideEnabled() {
    final int numberOfInitialProperties = 10;
    final Properties properties =
        IntStream.range(0, numberOfInitialProperties)
            .boxed()
            .collect(
                Collectors.toMap(
                    i -> String.format("property-%d", i),
                    i -> String.format("value-%d", i),
                    (a, b) -> b,
                    Properties::new));
    final Map<String, String> additionalProperties = new HashMap<>();
    additionalProperties.put("missing-property-1", "missing-value-1");
    additionalProperties.put(
        properties.keySet().iterator().next().toString(), "overridden-value-1");

    final Properties extendedProperties =
        KafkaHelper.extendWith(properties, additionalProperties, true);
    assertThat(extendedProperties)
        .containsAllEntriesOf(properties)
        .containsAllEntriesOf(additionalProperties);
  }
}
