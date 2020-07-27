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
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.util.List;

import org.apache.tuweni.bytes.Bytes32;

public interface EventStreamConfiguration {

  String getBrokerUrl();

  String getTopic();

  boolean isEnabled();

  boolean isMetadataDBEnabled();

  List<Address> getLogFilterAddresses();

  List<List<Bytes32>> getLogFilterTopics();

  EventSchemas getEventSchemas();

  File getEventSchemasFile();

  void loadEventSchemas();
}
