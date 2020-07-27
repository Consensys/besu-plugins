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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;

public class BesuNode {

  public static final String PROCESS_LOG_FILENAME = "subprocess.log";

  private static final Logger LOG = LogManager.getLogger();
  private static final Logger PROCESS_LOG =
      LogManager.getLogger("org.hyperledger.besu.SubProcessLog");

  private final String name;
  private final Process process;
  private final Path dataPath;
  private final ExecutorService outputProcessorExecutor = Executors.newCachedThreadPool();
  private final FileOutputStream logStream;
  private final Properties portsProperties = new Properties();

  private Web3j web3j;

  public BesuNode(
      final String name,
      final Process process,
      final Path dataPath,
      final FileOutputStream logStream) {
    this.name = name;
    this.process = process;
    this.dataPath = dataPath;
    this.logStream = logStream;
    outputProcessorExecutor.execute(this::printOutput);
  }

  private void printOutput() {
    try (final BufferedReader in =
        new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
      String line = in.readLine();
      while (line != null) {
        PROCESS_LOG.info("{}: {}", name, line);
        logStream.write(String.format("%s%n", line).getBytes(UTF_8));
        logStream.flush();
        try {
          line = in.readLine();
        } catch (final IOException e) {
          if (process.isAlive()) {
            LOG.error(
                "Reading besu output stream failed even though the process is still alive ({})",
                e.getMessage());
          }
          return;
        }
      }
    } catch (final IOException e) {
      LOG.error("Failed to read output from process", e);
    }
  }

  public Path getDataPath() {
    return dataPath;
  }

  public String getP2pPort() {
    return getPortByName("p2p");
  }

  public String getDiscPort() {
    return getPortByName("discovery");
  }

  public String getJsonRpcPort() {
    return getPortByName("json-rpc");
  }

  private String getPortByName(final String name) {
    return Optional.ofNullable(portsProperties.getProperty(name))
        .orElseThrow(
            () -> new IllegalStateException("Requested Port before ports properties were written"));
  }

  public boolean logContains(final String expectedContent) {
    try {
      return Files.readAllLines(getLogPath()).stream()
          .anyMatch(line -> line.contains(expectedContent));
    } catch (final IOException e) {
      throw new RuntimeException("Unable to extract lines from log file");
    }
  }

  public Path getLogPath() {
    return dataPath.resolve(PROCESS_LOG_FILENAME);
  }

  public void ensureHasTerminated() {
    Awaitility.waitAtMost(60, TimeUnit.SECONDS).until(() -> !process.isAlive());
  }

  public void waitToBeOperational() {
    final int secondsToWait = Boolean.getBoolean("debugSubProcess") ? 3600 : 60;

    final File file = new File(dataPath.toFile(), "besu.ports");
    Awaitility.waitAtMost(secondsToWait, TimeUnit.SECONDS)
        .until(
            () -> {
              if (file.exists()) {
                final boolean containsContent;
                try (final Stream<String> s = Files.lines(file.toPath())) {
                  containsContent = s.count() > 0;
                }
                if (containsContent) {
                  loadPortsFile();
                }
                return containsContent;
              } else {
                return false;
              }
            });
    final String web3jEndPointURL = String.format("http://localhost:%s", getJsonRpcPort());
    final Web3jService web3jService = new HttpService(web3jEndPointURL);
    web3j = new JsonRpc2_0Web3j(web3jService);
  }

  public void terminate() {
    Awaitility.waitAtMost(30, TimeUnit.SECONDS)
        .until(
            () -> {
              if (process.isAlive()) {
                process.destroy();
                outputProcessorExecutor.shutdown();
                return false;
              } else {
                return true;
              }
            });
  }

  public void waitForLogToContain(final String requiredText) {
    final int secondsToWait = Boolean.getBoolean("debugSubProcess") ? 3600 : 60;
    Awaitility.waitAtMost(secondsToWait, TimeUnit.SECONDS)
        .until(() -> this.logContains(requiredText));
  }

  private void loadPortsFile() {
    try (final FileInputStream fis =
        new FileInputStream(new File(dataPath.toFile(), "besu.ports"))) {
      portsProperties.load(fis);
      LOG.info("Ports for node {}: {}", name, portsProperties);
    } catch (final IOException e) {
      throw new RuntimeException("Error reading Besu ports file", e);
    }
  }

  public BigInteger blockNumber() {
    if (web3j == null) {
      throw new RuntimeException("Requested block number prior to initializing web3j.");
    }

    try {
      return web3j.ethBlockNumber().send().getBlockNumber();
    } catch (final IOException e) {
      LOG.error("Failed to determine block number of besu.");
      throw new RuntimeException(e);
    }
  }

  public BigInteger peerCount() {
    if (web3j == null) {
      throw new RuntimeException("Requested peer count prior to initializing web3j.");
    }

    try {
      return web3j.netPeerCount().send().getQuantity();
    } catch (final Exception e) {
      LOG.error(name + ": Failed to determine peer count of besu.", e);
      throw new RuntimeException(e);
    }
  }

  public void waitToHaveExpectedPeerCount(final int expectedPeerCount) {
    Awaitility.setDefaultPollInterval(500, TimeUnit.MILLISECONDS);
    Awaitility.waitAtMost(60, TimeUnit.SECONDS)
        .until(
            () -> {
              final BigInteger peerCount;
              try {
                peerCount = peerCount();
              } catch (final Exception e) {
                return false;
              }
              return peerCount.compareTo(BigInteger.valueOf(expectedPeerCount)) >= 0;
            });
  }
}
