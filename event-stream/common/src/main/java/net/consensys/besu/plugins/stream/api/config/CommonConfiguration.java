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
package net.consensys.besu.plugins.stream.api.config;

import net.consensys.besu.plugins.stream.core.config.EventSchemas;
import net.consensys.besu.plugins.stream.core.config.LogFilterTopicsWrapper;
import net.consensys.besu.plugins.stream.model.DomainObjectType;
import net.consensys.besu.plugins.types.Address;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;

public class CommonConfiguration implements EventStreamConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

  protected boolean enabled = false;
  protected String topic = "pegasys-stream";
  protected String brokerUrl = "127.0.0.1:9092";
  protected boolean metadataDBEnabled = true;
  protected List<Address> logFilterAddresses = new ArrayList<>();
  protected LogFilterTopicsWrapper logFilterTopicsWrapper = LogFilterTopicsWrapper.empty();
  protected Optional<List<DomainObjectType>> enabledTopics = Optional.empty();
  protected File eventSchemasFile;

  private EventSchemas eventSchemas = EventSchemas.empty();

  @Override
  public String getBrokerUrl() {
    return brokerUrl;
  }

  @Override
  public String getTopic() {
    return topic;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isMetadataDBEnabled() {
    return metadataDBEnabled;
  }

  @Override
  public List<DomainObjectType> getEnabledTopics() {
    return enabledTopics.orElse(Arrays.asList(DomainObjectType.values()));
  }

  @Override
  public List<Address> getLogFilterAddresses() {
    return logFilterAddresses;
  }

  @Override
  public List<List<Bytes32>> getLogFilterTopics() {
    return logFilterTopicsWrapper.getTopics();
  }

  @Override
  public EventSchemas getEventSchemas() {
    return eventSchemas;
  }

  @Override
  public File getEventSchemasFile() {
    return eventSchemasFile;
  }

  @Override
  public void loadEventSchemas() {
    this.eventSchemas =
        Optional.ofNullable(getEventSchemasFile())
            .map(
                eventSchemasFile -> {
                  try {
                    return EventSchemas.from(getEventSchemasFile());
                  } catch (IOException e) {
                    LOGGER.error(e);
                    return eventSchemas;
                  }
                })
            .orElse(eventSchemas);
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setTopic(final String topic) {
    this.topic = topic;
  }

  public void setBrokerUrl(String brokerUrl) {
    this.brokerUrl = brokerUrl;
  }

  public void setMetadataDBEnabled(boolean metadataDBEnabled) {
    this.metadataDBEnabled = metadataDBEnabled;
  }

  public void setEnabledTopics(List<DomainObjectType> enabledTopics) {
    this.enabledTopics = Optional.of(enabledTopics);
  }

  public void setLogFilterTopicsWrapper(LogFilterTopicsWrapper logFilterTopicsWrapper) {
    this.logFilterTopicsWrapper = logFilterTopicsWrapper;
  }

  public void setLogFilterAddresses(List<Address> logFilterAddresses) {
    this.logFilterAddresses = logFilterAddresses;
  }

  public void setEventSchemasFile(File eventSchemasFile) {
    this.eventSchemasFile = eventSchemasFile;
  }
}
