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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.Lists;

public class BesuNodeFactory {

  private static final File runDirectory = new File(System.getProperty("user.dir")).getParentFile();
  private static final Path executablePath =
      Path.of(runDirectory.getAbsolutePath())
          .resolve(Path.of("build", "distributions", "install", "besu-plugins", "bin", "besu"));

  public BesuNode create(final BesuNodeConfig config) {
    final Path dataPath = determineDataPath(config);

    final List<String> params = Lists.newArrayList();
    params.add(executablePath.toString());
    if (config.getGenesisFile().isEmpty()) {
      params.add("--network");
      params.add("DEV");
    } else {
      params.add("--genesis-file");
      params.add(config.getGenesisFile().get().toString());
    }
    params.add("--data-path");
    params.add(dataPath.toString());
    params.add("--logging=INFO");
    params.add("--p2p-port=0");
    params.add("--rpc-http-enabled=true");
    params.add("--rpc-http-port=0");
    params.addAll(config.getAdditionalCommandLineArgs());

    final Process process = createBesuProcess(runDirectory, params, config);

    final FileOutputStream logStream = createLogStream(dataPath);

    return new BesuNode(config.getName(), process, dataPath, logStream);
  }

  private Path determineDataPath(final BesuNodeConfig config) {
    if (config.getDataPath().isEmpty()) {
      try {
        return Files.createTempDirectory("");
      } catch (final IOException e) {
        throw new RuntimeException("Failed to create a data directory for the node");
      }
    } else {
      return config.getDataPath().get();
    }
  }

  private Process createBesuProcess(
      final File runDirectory, final List<String> commandLine, final BesuNodeConfig config) {

    final ProcessBuilder processBuilder =
        new ProcessBuilder(commandLine)
            .directory(runDirectory)
            .redirectErrorStream(true)
            .redirectInput(Redirect.INHERIT);

    if (!config.usePlugins()) {
      processBuilder.environment().put("BESU_OPTS", "-Dbesu.plugins.dir=./");
    }

    for (final String envVarToRemove : config.getEnvironmentVariablesToRemove()) {
      processBuilder.environment().remove(envVarToRemove);
    }

    final StringBuilder javaOptions = new StringBuilder();

    if (Boolean.getBoolean("debugSubProcess")) {
      javaOptions.append(
          "-Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
      javaOptions.append(" ");
      processBuilder.environment().put("JAVA_OPTS", javaOptions.toString());
    }

    try {
      return processBuilder.start();
    } catch (final IOException e) {
      throw new RuntimeException("Failed to start the Besu application.", e);
    }
  }

  private FileOutputStream createLogStream(final Path dataPath) {
    try {
      return new FileOutputStream(dataPath.resolve(BesuNode.PROCESS_LOG_FILENAME).toFile());
    } catch (final FileNotFoundException e) {
      throw new RuntimeException("Unable to create the subprocess log.", e);
    }
  }
}
