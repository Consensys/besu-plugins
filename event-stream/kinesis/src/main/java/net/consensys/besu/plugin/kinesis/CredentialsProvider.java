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
package net.consensys.besu.plugin.kinesis;

import java.util.function.Supplier;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

public class CredentialsProvider {

  static AwsCredentialsProvider of(
      final Supplier<String> accessIdSupplier, final Supplier<String> secretKeySupplier) {
    final String accessId = accessIdSupplier.get();
    final String secretKey = secretKeySupplier.get();
    if (accessId == null || accessId.isBlank() || secretKey == null || secretKey.isBlank()) {
      return DefaultCredentialsProvider.create();
    } else {
      return () ->
          new AwsCredentials() {
            @Override
            public String accessKeyId() {
              return accessId;
            }

            @Override
            public String secretAccessKey() {
              return secretKey;
            }
          };
    }
  }
}
