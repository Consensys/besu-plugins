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
package net.consensys.besu.plugins.dsl.cli;

import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class ExceptionHandler extends CommandLine.AbstractHandler<List<Object>, ExceptionHandler>
    implements CommandLine.IExceptionHandler2<List<Object>> {

  @Override
  public List<Object> handleParseException(final ParameterException ex, final String[] args) {
    throw ex;
  }

  @Override
  public List<Object> handleExecutionException(
      final CommandLine.ExecutionException ex, final CommandLine.ParseResult parseResult) {
    return throwOrExit(ex);
  }

  @Override
  protected ExceptionHandler self() {
    return this;
  }
}
