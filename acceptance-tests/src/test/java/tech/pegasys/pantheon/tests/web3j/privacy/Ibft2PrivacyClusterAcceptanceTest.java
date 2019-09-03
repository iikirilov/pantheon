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
package tech.pegasys.pantheon.tests.web3j.privacy;

import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyAcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;

import org.junit.Before;

public class Ibft2PrivacyClusterAcceptanceTest extends PrivacyAcceptanceTestBase {
  //  private static final String CONTRACT_NAME = "Event Emmiter";
  //
  //  private EventEmitterHarness eventEmitterHarness;
  //  private PrivacyNet privacyNet;
  //
  //  @Before
  //  public void setUp() throws Exception {
  //    privacyNet =
  //        PrivacyNet.builder(privacy, privacyPantheon, cluster, false)
  //            .addMinerNode("Alice")
  //            .addMinerNode("Bob")
  //            .addMinerNode("Charlie")
  //            .build();
  //    privacyNet.startPrivacyNet();
  //    eventEmitterHarness =
  //        new EventEmitterHarness(
  //            privateTransactionBuilder,
  //            privacyNet,
  //            privateTransactions,
  //            privateTransactionVerifier,
  //                privacyConditions);
  //  }

  private PrivacyNode alice;
  private PrivacyNode bob;
  private PrivacyNode charlie;

  @Before
  public void setUp() throws Exception {
    alice = privacyPantheon.createIbft2NodePrivacyEnabled("node1", privacyAccountSupplier.get());
    bob = privacyPantheon.createIbft2NodePrivacyEnabled("node2", privacyAccountSupplier.get());
    charlie = privacyPantheon.createIbft2NodePrivacyEnabled("node3", privacyAccountSupplier.get());
    privacyCluster.start(alice, bob, charlie);
  }
  //
  //  @Test
  //  public void node2CanSeeContract() {
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob");
  //  }
  //
  //  @Test
  //  public void node2CanExecuteContract() {
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob");
  //    eventEmitterHarness.store(CONTRACT_NAME, "Bob", "Alice");
  //  }
  //
  //  @Test
  //  public void node2CanSeePrivateTransactionReceipt() {
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob");
  //    eventEmitterHarness.store(CONTRACT_NAME, "Bob", "Alice");
  //    eventEmitterHarness.get(CONTRACT_NAME, "Bob", "Alice");
  //  }
  //
  //  @Test(expected = RuntimeException.class)
  //  public void node2ExpectError() {
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob");
  //
  //    String invalidStoreValueFromNode2 =
  //        PrivateTransactionBuilder.builder()
  //            .nonce(0)
  //            .from(privacyNet.getNode("Bob").getAddress())
  //
  // .to(Address.fromHexString(eventEmitterHarness.resolveContractAddress(CONTRACT_NAME)))
  //            .privateFrom(
  //                BytesValue.wrap(
  //                    privacyNet
  //                        .getEnclave("Alice")
  //                        .getPublicKeys()
  //                        .get(0)
  //                        .getBytes(UTF_8))) // wrong public key
  //            .privateFor(
  //                Lists.newArrayList(
  //                    BytesValue.wrap(
  //                        privacyNet.getEnclave("Bob").getPublicKeys().get(0).getBytes(UTF_8))))
  //            .keyPair(privacyNet.getNode("Bob").pantheon.keyPair())
  //            .build(TransactionType.STORE);
  //
  //    privacyNet
  //        .getNode("Bob")
  //        .execute(privateTransactions.createPrivateRawTransaction(invalidStoreValueFromNode2));
  //  }
  //
  //  @Test
  //  public void node1CanDeployMultipleTimes() {
  //
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob");
  //    eventEmitterHarness.store(CONTRACT_NAME, "Bob", "Alice");
  //
  //    final String secondContract = "Event Emitter 2";
  //
  //    eventEmitterHarness.deploy(secondContract, "Alice", "Bob");
  //    eventEmitterHarness.store(secondContract, "Bob", "Alice");
  //  }
  //
  //  @Test
  //  public void node1CanInteractWithMultiplePrivacyGroups() {
  //
  //    eventEmitterHarness.deploy(CONTRACT_NAME, "Alice", "Bob", "Charlie");
  //    eventEmitterHarness.store(CONTRACT_NAME, "Alice", "Bob", "Charlie");
  //
  //    final String secondContract = "Event Emitter 2";
  //
  //    eventEmitterHarness.store(
  //        secondContract,
  //        privateContractVerifier.noValidEventReturned(),
  //            privateContractVerifier.noValidEventReturned(),
  //        "Alice",
  //        "Bob");
  //    eventEmitterHarness.deploy(secondContract, "Alice", "Bob");
  //    eventEmitterHarness.store(secondContract, "Alice", "Bob");
  //    eventEmitterHarness.get(secondContract, "Alice", "Bob");
  //  }
  //
  //  @After
  //  public void tearDown() {
  //    privacyNet.stopPrivacyNet();
  //  }
}
