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

import net.consensys.besu.plugins.stream.model.DomainObjectType;

import picocli.CommandLine.ITypeConverter;

/** Converter for domain object type on CLI */
public class DomainObjectTypeConverter implements ITypeConverter<DomainObjectType> {

  @Override
  public DomainObjectType convert(String value) throws Exception {
    return DomainObjectType.valueOf(value.toUpperCase());
  }
}
