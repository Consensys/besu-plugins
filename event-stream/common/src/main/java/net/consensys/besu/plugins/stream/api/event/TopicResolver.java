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

import java.util.function.Supplier;

/** Topic Resolver functional interface */
@FunctionalInterface
public interface TopicResolver {
  /**
   * Resolve the given object type to a topic
   *
   * @param domainObjectType the type corresponding to a topic
   * @param event currently ignored
   * @return A string representing the topic, if successfully resolved
   */
  String resolve(final DomainObjectType domainObjectType, final Event event);

  /** Fixed topic resolver */
  class Fixed implements TopicResolver {
    private Supplier<String> topicSupplier;

    /**
     * create a new fixed topic resolver
     *
     * @param topicSupplier supplies the topic
     */
    public Fixed(final Supplier<String> topicSupplier) {
      this.topicSupplier = topicSupplier;
    }

    /**
     * resolve the topic from the given inputs
     *
     * @param domainObjectType the type corresponding to a topic, currently ignored
     * @param event currently ignored
     * @return the string representing the topic
     */
    @Override
    public String resolve(final DomainObjectType domainObjectType, final Event event) {
      return topicSupplier.get();
    }
  }
}
