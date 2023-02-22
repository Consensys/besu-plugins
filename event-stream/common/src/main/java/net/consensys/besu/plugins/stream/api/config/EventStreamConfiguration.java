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
package net.consensys.besu.plugins.stream.api.config;

import net.consensys.besu.plugins.stream.core.config.EventSchemas;
import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.List;

import org.apache.tuweni.bytes.Bytes32;

/** Event stream configuration interface */
public interface EventStreamConfiguration {

  /**
   * get the broker URL
   *
   * @return the broker URL
   */
  String getBrokerUrl();

  /**
   * get the topic
   *
   * @return the topic
   */
  String getTopic();

  /**
   * is the stream enabled
   *
   * @return whether the stream configuration is enabled
   */
  boolean isEnabled();

  /**
   * is the metadata DB enabled
   *
   * @return whether the metadata DB is enabled
   */
  boolean isMetadataDBEnabled();

  /**
   * return the enabled topics
   *
   * @return the enabled topics
   */
  List<DomainObjectType> getEnabledTopics();

  /**
   * return the log filter addresses
   *
   * @return the log filter addresses
   */
  List<Address> getLogFilterAddresses();

  /**
   * return the log filter topics
   *
   * @return the log filter topics
   */
  List<List<Bytes32>> getLogFilterTopics();

  /**
   * return the event schemas
   *
   * @return the event schemas
   */
  EventSchemas getEventSchemas();

  /**
   * return the event schemas file
   *
   * @return the event schemas file
   */
  File getEventSchemasFile();

  /** load the schemas */
  void loadEventSchemas();
}
