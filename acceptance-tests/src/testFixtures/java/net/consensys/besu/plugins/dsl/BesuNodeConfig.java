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

import static java.util.Collections.emptyList;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class BesuNodeConfig {

  private final Optional<Path> dataPath;
  private final boolean usePlugins;
  private final String name;
  private final List<String> additionalCommandLineArgs;
  private final List<String> envVarsToRemove;
  private final Optional<Path> genesisFile;

  public BesuNodeConfig(
      final String name,
      final Path dataPath,
      final Path genesisFile,
      final boolean usePlugins,
      final List<String> additionalCommandLineArgs,
      final List<String> envVarsToRemove) {
    this.dataPath = Optional.ofNullable(dataPath);
    this.genesisFile = Optional.ofNullable(genesisFile);
    this.usePlugins = usePlugins;
    this.name = name;
    this.additionalCommandLineArgs = additionalCommandLineArgs;
    this.envVarsToRemove = envVarsToRemove;
  }

  public BesuNodeConfig(final String name, final Path dataPath, final boolean usePlugins) {
    this(name, dataPath, null, usePlugins, emptyList(), emptyList());
  }

  public Optional<Path> getDataPath() {
    return dataPath;
  }

  public boolean usePlugins() {
    return usePlugins;
  }

  public String getName() {
    return name;
  }

  public Optional<Path> getGenesisFile() {
    return genesisFile;
  }

  public List<String> getAdditionalCommandLineArgs() {
    return additionalCommandLineArgs;
  }

  public List<String> getEnvironmentVariablesToRemove() {
    return envVarsToRemove;
  }
}
