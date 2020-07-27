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
package net.consensys.besu.plugins.dsl;

import java.util.List;

public class BesuNetwork {

  final List<BesuNode> members;

  public BesuNetwork(List<BesuNode> members) {
    this.members = members;
  }

  public void waitForAllNodesToConnect() {
    final int expectedPeers = members.size() - 1;
    for (final BesuNode node : members) {
      node.waitToHaveExpectedPeerCount(expectedPeers);
    }
  }

  public void shutdown() {
    for (final BesuNode node : members) {
      node.terminate();
    }
  }

  public BesuNode getNode(int index) {
    return members.get(index);
  }
}
