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
package net.consensys.besu.plugins.stream.core;

import net.consensys.besu.plugins.stream.api.event.Event;
import net.consensys.besu.plugins.stream.api.event.TopicResolver;
import net.consensys.besu.plugins.stream.model.DomainObjectType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DomainObjectTopicResolver implements TopicResolver {

  private final String prefix;
  private static final String DEFAULT_TOPIC = "default";
  private final Map<DomainObjectType, String> topics = new HashMap<>();

  public DomainObjectTopicResolver(final Supplier<String> prefixSupplier) {
    this(prefixSupplier.get());
  }

  private DomainObjectTopicResolver(final String prefix) {
    this.prefix = prefix;
    buildTopics();
  }

  private void buildTopics() {
    for (DomainObjectType domainObjectType : DomainObjectType.values()) {
      topics.put(domainObjectType, prefix.concat(domainObjectType.getName()));
    }
  }

  @Override
  public String resolve(final DomainObjectType domainObjectType, final Event ignored) {
    return topics.getOrDefault(domainObjectType, DEFAULT_TOPIC);
  }
}
