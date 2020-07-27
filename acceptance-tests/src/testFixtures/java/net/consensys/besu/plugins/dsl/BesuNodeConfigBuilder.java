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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class BesuNodeConfigBuilder {

  private Path dataPath;
  private boolean usePlugins = true;
  private String name = "besu-plugins";
  private Path genesisFile;

  private List<String> additionalCommandLineArgs = Collections.emptyList();
  private List<String> envVarsToRemove = Collections.emptyList();

  public BesuNodeConfigBuilder dataPath(Path dataPath) {
    this.dataPath = dataPath;
    return this;
  }

  public BesuNodeConfigBuilder useplugins(boolean usePlugins) {
    this.usePlugins = usePlugins;
    return this;
  }

  public BesuNodeConfigBuilder name(String name) {
    this.name = name;
    return this;
  }

  public BesuNodeConfigBuilder genesisFile(Path genesisFile) {
    this.genesisFile = genesisFile;
    return this;
  }

  public BesuNodeConfigBuilder additionalCommandLineArgs(List<String> additionalCommandLineArgs) {
    this.additionalCommandLineArgs = additionalCommandLineArgs;
    return this;
  }

  public BesuNodeConfigBuilder envVarsToRemove(String... envVarsToRemove) {
    this.envVarsToRemove = Lists.newArrayList(envVarsToRemove);
    return this;
  }

  public BesuNodeConfig build() {
    return new BesuNodeConfig(
        name, dataPath, genesisFile, usePlugins, additionalCommandLineArgs, envVarsToRemove);
  }
}
