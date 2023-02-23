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
package net.consensys.besu.plugins.stream.model;

/** enumeration of object types */
public enum DomainObjectType {
  /** block */
  BLOCK("block"),
  /** transaction */
  TRANSACTION("transaction"),
  /** smart-contract */
  SMART_CONTRACT("smart-contract"),
  /** node */
  NODE("node"),
  /** log */
  LOG("log");

  private final String name;

  DomainObjectType(final String name) {
    this.name = name;
  }

  /**
   * The name of the object type
   *
   * @return A string representing the name of the object type
   */
  public String getName() {
    return name;
  }
}
