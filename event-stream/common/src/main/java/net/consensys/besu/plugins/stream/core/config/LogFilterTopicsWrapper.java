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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.tuweni.bytes.Bytes32;

public class LogFilterTopicsWrapper {

  private final List<List<Bytes32>> topics;

  private LogFilterTopicsWrapper(final List<List<Bytes32>> topics) {
    this.topics = topics;
  }

  public static LogFilterTopicsWrapper parse(final String input) throws IOException {
    final JsonNode topicsNode =
        new ObjectMapper()
            .readTree(new ObjectMapper().getFactory().createParser(ensureJSONArray(input)));
    final List<List<Bytes32>> topics = Lists.newArrayList();

    if (!topicsNode.isArray()) {
      topics.add(singletonList(Bytes32.fromHexString(topicsNode.textValue())));
      return new LogFilterTopicsWrapper(topics);
    }

    for (final JsonNode child : topicsNode) {
      if (child.isNull()) {
        topics.add(null);
      } else if (child.isArray()) {
        final List<Bytes32> childItems = Lists.newArrayList();
        for (final JsonNode subChild : child) {
          childItems.add(Bytes32.fromHexString(subChild.textValue()));
        }
        topics.add(childItems);
      } else {
        topics.add(
            singletonList(net.consensys.besu.plugins.types.Hash.fromHexString(child.textValue())));
      }
    }

    return new LogFilterTopicsWrapper(topics);
  }

  public static LogFilterTopicsWrapper empty() {
    return new LogFilterTopicsWrapper(emptyList());
  }

  public List<List<Bytes32>> getTopics() {
    return topics;
  }

  private static String ensureJSONArray(final String input) {
    return input.replaceAll("\"", "").replaceAll("(0x[a-fA-F0-9]+)", "\"$1\"");
  }
}
