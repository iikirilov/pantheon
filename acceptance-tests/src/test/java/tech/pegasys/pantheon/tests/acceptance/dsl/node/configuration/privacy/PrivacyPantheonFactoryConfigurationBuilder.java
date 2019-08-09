/*
 * Copyright 2018 ConsenSys AG.
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
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.PantheonFactoryConfiguration;

public class PrivacyPantheonFactoryConfigurationBuilder {

  protected PantheonFactoryConfiguration pantheonConfig;
  protected OrionFactoryConfiguration orionConfig;

  public PrivacyPantheonFactoryConfigurationBuilder setPantheonConfig(
      final PantheonFactoryConfiguration pantheonConfig) {
    this.pantheonConfig = pantheonConfig;
    return this;
  }

  public PrivacyPantheonFactoryConfigurationBuilder setOrionConfig(
      final OrionFactoryConfiguration orionConfig) {
    this.orionConfig = orionConfig;
    return this;
  }

  public PrivacyPantheonFactoryConfiguration build() {
    return new PrivacyPantheonFactoryConfiguration(
        pantheonConfig.getName(),
        pantheonConfig.getMiningParameters(),
        pantheonConfig.getPrivacyParameters(),
        pantheonConfig.getJsonRpcConfiguration(),
        pantheonConfig.getWebSocketConfiguration(),
        pantheonConfig.getMetricsConfiguration(),
        pantheonConfig.getPermissioningConfiguration(),
        pantheonConfig.getKeyFilePath(),
        pantheonConfig.isDevMode(),
        pantheonConfig.getGenesisConfigProvider(),
        pantheonConfig.isP2pEnabled(),
        pantheonConfig.getNetworkingConfiguration(),
        pantheonConfig.isDiscoveryEnabled(),
        pantheonConfig.isBootnodeEligible(),
        pantheonConfig.isRevertReasonEnabled(),
        pantheonConfig.getPlugins(),
        pantheonConfig.getExtraCLIOptions());
  }
}
