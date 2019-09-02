/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.account;

public class PrivacyAccount {

  private final String privateKeyPath;
  private final String enclaveKeyPath;
  private final String enclavePrivateKeyPath;

  private PrivacyAccount(
      final String privateKeyPath,
      final String enclavePublicKeyPath,
      final String enclavePrivateKeyPath) {
    this.privateKeyPath = privateKeyPath;
    this.enclaveKeyPath = enclavePublicKeyPath;
    this.enclavePrivateKeyPath = enclavePrivateKeyPath;
  }

  public static PrivacyAccount create(
      final String privateKeyPath,
      final String enclavePublicKeyPath,
      final String enclavePrivateKeyPath) {
    return new PrivacyAccount(privateKeyPath, enclavePublicKeyPath, enclavePrivateKeyPath);
  }

  public String getPrivateKeyPath() {
    return privateKeyPath;
  }

  public String getEnclaveKeyPath() {
    return enclaveKeyPath;
  }

  public String getEnclavePrivateKeyPath() {
    return enclavePrivateKeyPath;
  }
}
