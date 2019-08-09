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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import tech.pegasys.orion.testutil.OrionFactoryConfiguration;
import tech.pegasys.orion.testutil.OrionTestHarness;
import tech.pegasys.orion.testutil.OrionTestHarnessFactory;
import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.types.SendRequest;
import tech.pegasys.pantheon.enclave.types.SendRequestLegacy;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNodeRunner;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.RunnableNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.NodeConfiguration;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.PantheonFactoryConfiguration;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.priv.PrivGetTransactionCountTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrivacyNode implements RunnableNode, AutoCloseable {
  private static final Logger LOG = LogManager.getLogger();

  public OrionTestHarness orion;
  public PantheonNode pantheon;

  public PrivacyNode(
      final PantheonFactoryConfiguration pantheonConfig,
      final OrionFactoryConfiguration orionConfig)
      throws IOException {
    this.orion = OrionTestHarnessFactory.create(orionConfig);

    this.pantheon =
        new PantheonNode(
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
            pantheonConfig.getExtraCLIOptions(),
            new ArrayList<>());
  }

  public BytesValue getOrionPubKeyBytes() {
    return BytesValue.wrap(orion.getPublicKeys().get(0).getBytes(UTF_8));
  }

  public void testOrionConnection(final PrivacyNode... otherNodes) {
    LOG.info(
        String.format(
            "Testing Orion connectivity between %s (%s) and %s (%s)",
            pantheon.getName(),
            orion.nodeUrl(),
            Arrays.toString(
                Arrays.stream(otherNodes).map(node -> node.pantheon.getName()).toArray()),
            Arrays.toString(
                Arrays.stream(otherNodes).map(node -> node.orion.nodeUrl()).toArray())));
    Enclave orionEnclave = new Enclave(orion.clientUrl());
    SendRequest sendRequest1 =
        new SendRequestLegacy(
            "SGVsbG8sIFdvcmxkIQ==",
            orion.getPublicKeys().get(0),
            Arrays.stream(otherNodes)
                .map(node -> node.orion.getPublicKeys().get(0))
                .collect(Collectors.toList()));
    waitFor(() -> orionEnclave.send(sendRequest1));
  }

  public long nextNonce(final BytesValue privacyGroupId) {
    return pantheon
        .execute(
            new PrivGetTransactionCountTransaction(
                pantheon.getAddress().toString(), BytesValues.asBase64String(privacyGroupId)))
        .longValue();
  }

  @Override
  public void stop() {
    pantheon.stop();
    orion.stop();
  }

  @Override
  public void close() {
    pantheon.close();
    orion.close();
  }

  @Override
  public void start(PantheonNodeRunner runner) {
    orion.start();

    final PrivacyParameters privacyParameters;
    try {
      privacyParameters =
          new PrivacyParameters.Builder()
              .setEnabled(true)
              .setEnclaveUrl(orion.clientUrl())
              .setEnclavePublicKeyUsingFile(orion.getConfig().publicKeys().get(0).toFile())
              .setDataDir(Files.createTempDirectory("acctest-privacy"))
              .build();
    } catch (IOException e) {
      throw new RuntimeException();
    }
    pantheon.setPrivacyParameters(privacyParameters);
    pantheon.start(runner);
  }

  @Override
  public NodeConfiguration getConfiguration() {
    return pantheon.getConfiguration();
  }

  @Override
  public void awaitPeerDiscovery(Condition condition) {
    pantheon.awaitPeerDiscovery(condition);
  }

  @Override
  public String getName() {
    return pantheon.getName();
  }

  @Override
  public Address getAddress() {
    return pantheon.getAddress();
  }

  @Override
  public URI enodeUrl() {
    return pantheon.enodeUrl();
  }

  @Override
  public String getNodeId() {
    return pantheon.getNodeId();
  }

  @Override
  public <T> T execute(Transaction<T> transaction) {
    return pantheon.execute(transaction);
  }

  @Override
  public void verify(Condition expected) {
    pantheon.verify(expected);
  }
}
