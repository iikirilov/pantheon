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
package tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.privacy;

import tech.pegasys.orion.testutil.OrionFactoryConfiguration;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.NodeConfigurationFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.PantheonFactoryConfiguration;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.PantheonFactoryConfigurationBuilder;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.genesis.GenesisConfigurationFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;

import java.io.IOException;

public class PrivacyPantheonNodeFactory {

  private final GenesisConfigurationFactory genesis = new GenesisConfigurationFactory();
  private final NodeConfigurationFactory node = new NodeConfigurationFactory();

  private static PrivacyNode create(
      final PantheonFactoryConfiguration pantheonConfig,
      final OrionFactoryConfiguration orionConfig)
      throws IOException {
    return new PrivacyNode(pantheonConfig, orionConfig);
  }

  public PrivacyNode createPrivateTransactionEnabledMinerNode(
      final String name, final String transactionSigningKeyPath) throws IOException {
    return create(
        new PantheonFactoryConfigurationBuilder()
            .name(name)
            .miningEnabled()
            .jsonRpcEnabled()
            .webSocketEnabled()
            .enablePrivateTransactions()
            .keyFilePath(transactionSigningKeyPath)
            .build(),
        new OrionFactoryConfiguration("orion_key_0.pub", "orion_key_0.key"));
  }

  public PrivacyNode createPrivateTransactionEnabledMinerNode(
      final String name,
      final PrivacyParameters privacyParameters,
      final String keyFilePath,
      final OrionFactoryConfiguration orionTestHarness)
      throws IOException {
    return create(
        new PrivacyPantheonFactoryConfigurationBuilder()
            .setPantheonConfig(
                new PantheonFactoryConfigurationBuilder()
                    .name(name)
                    .miningEnabled()
                    .jsonRpcEnabled()
                    .keyFilePath(keyFilePath)
                    .enablePrivateTransactions()
                    .webSocketEnabled()
                    .build())
            .build(),
        orionTestHarness);
  }

  public PrivacyNode createPrivateTransactionEnabledNode(
      final String name,
      final PrivacyParameters privacyParameters,
      final String keyFilePath,
      final OrionFactoryConfiguration orionTestHarness)
      throws IOException {
    return create(
        new PrivacyPantheonFactoryConfigurationBuilder()
            .setPantheonConfig(
                new PantheonFactoryConfigurationBuilder()
                    .name(name)
                    .jsonRpcEnabled()
                    .keyFilePath(keyFilePath)
                    .enablePrivateTransactions()
                    .webSocketEnabled()
                    .build())
            .build(),
        orionTestHarness);
  }

  public PrivacyNode createIbft2NodePrivacyEnabled(
      final String name,
      final PrivacyParameters privacyParameters,
      final String keyFilePath,
      final OrionFactoryConfiguration orionTestHarness)
      throws IOException {
    return create(
        new PrivacyPantheonFactoryConfigurationBuilder()
            .setPantheonConfig(
                new PantheonFactoryConfigurationBuilder()
                    .name(name)
                    .miningEnabled()
                    .jsonRpcConfiguration(node.createJsonRpcWithIbft2EnabledConfig())
                    .webSocketConfiguration(node.createWebSocketEnabledConfig())
                    .devMode(false)
                    .genesisConfigProvider(genesis::createIbft2GenesisConfig)
                    .keyFilePath(keyFilePath)
                    .enablePrivateTransactions()
                    .build())
            .build(),
        orionTestHarness);
  }
}
