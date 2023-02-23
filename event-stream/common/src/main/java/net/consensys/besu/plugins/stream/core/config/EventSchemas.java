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
package net.consensys.besu.plugins.stream.core.config;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;

/** Represents a set of event schemas */
public class EventSchemas {
  private final Set<EventSchema> schemas;

  /**
   * constructs a new EventSchemas
   *
   * @param schemas the set of event schemas
   */
  @VisibleForTesting
  public EventSchemas(final Set<EventSchema> schemas) {
    this.schemas = schemas;
  }

  /**
   * create a new empty set of event schemas
   *
   * @return the empty EventSchemas
   */
  public static EventSchemas empty() {
    return new EventSchemas(emptySet());
  }

  /**
   * create a new set of event schemas from the given yaml file
   *
   * @param eventSchemasYamlFile a yaml file representing the event schemas definitions
   * @throws IOException if the file cannot be parsed
   * @return the newly created EventSchemas object
   */
  public static EventSchemas from(final File eventSchemasYamlFile) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    final EventSchemaConfigurationParseTarget eventSchemaConfigurationParseTarget =
        mapper.readValue(eventSchemasYamlFile, EventSchemaConfigurationParseTarget.class);
    return new EventSchemas(
        eventSchemaConfigurationParseTarget.getSingleEventParseTargets().stream()
            .map(EventSchema::from)
            .collect(toUnmodifiableSet()));
  }

  /**
   * getter for the set of EventSchema objects
   *
   * @return the Set of EventSchema objects
   */
  public Set<EventSchema> getSchemas() {
    return schemas;
  }

  @Override
  public boolean equals(final Object other) {
    try {
      return ((EventSchemas) other).getSchemas().equals(schemas);
    } catch (ClassCastException __) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemas);
  }
}
