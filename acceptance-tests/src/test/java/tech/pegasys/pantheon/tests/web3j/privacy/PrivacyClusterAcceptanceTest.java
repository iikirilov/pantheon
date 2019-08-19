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
import tech.pegasys.pantheon.tests.web3j.generated.EventEmitter;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.eea.response.PrivateTransactionReceipt;

public class PrivacyClusterAcceptanceTest extends PrivacyAcceptanceTestBase {

  private PrivacyNode alice;
  private PrivacyNode bob;
  private PrivacyNode charlie;

  @Before
  public void setUp() throws Exception {
    alice =
        privacyPantheon.createPrivateTransactionEnabledMinerNode(
            "node1", privacyAccountSupplier.get());
    bob =
        privacyPantheon.createPrivateTransactionEnabledNode("node2", privacyAccountSupplier.get());
    charlie =
        privacyPantheon.createPrivateTransactionEnabledNode("node3", privacyAccountSupplier.get());
    privacyCluster.start(alice, bob, charlie);
  }

  @Test
  public void onlyAliceAndBobCanExecuteContract() {
    // Contract address is generated from sender address and transaction nonce
    final String contractAddress = "0xebf56429e6500e84442467292183d4d621359838";

    final EventEmitter eventEmitter =
        alice.execute(
            privateContractTransactions.createSmartContractWithPrivacyGroupId(
                EventEmitter.class,
                alice.getTransactionSigningKey(),
                POW_CHAIN_ID,
                alice.getEnclaveKey(),
                bob.getEnclaveKey()));

    privateContractVerifier
        .validPrivateContractDeployed(contractAddress, alice.getAddress().toString())
        .verify(eventEmitter);

    final String transactionHash =
        alice.execute(
            privateContractTransactions.callSmartContract(
                contractAddress,
                eventEmitter.store(BigInteger.ONE).encodeFunctionCall(),
                alice.getTransactionSigningKey(),
                POW_CHAIN_ID,
                alice.getEnclaveKey(),
                bob.getEnclaveKey()));

    final PrivateTransactionReceipt expectedReceipt =
        alice.execute(privacyTransactions.getPrivateTransactionReceipt(transactionHash));

    privateTransactionVerifier
        .validPrivateTransactionReceipt(transactionHash, expectedReceipt)
        .verify(bob);
    privateTransactionVerifier.noPrivateTransactionReceipt(transactionHash).verify(charlie);
  }
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
  //        privateTransactionVerifier.noValidEventReturned(),
  //        privateTransactionVerifier.noValidEventReturned(),
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
