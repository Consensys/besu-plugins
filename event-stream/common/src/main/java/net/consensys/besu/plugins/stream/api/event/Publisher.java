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
package net.consensys.besu.plugins.stream.api.event;

import net.consensys.besu.plugins.stream.model.DomainObjectType;

/** functional interface for the publisher */
@FunctionalInterface
public interface Publisher {
  /**
   * publish the given event
   *
   * @param domainObjectType the type of event
   * @param topicResolver the topic resolver for the event
   * @param event the event to publish
   */
  void publish(
      final DomainObjectType domainObjectType,
      final TopicResolver topicResolver,
      final Event event);
}
