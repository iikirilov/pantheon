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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.contract;

public class PrivateContractVerifier {

  public PrivateContractVerifier() {}

  public ExpectValidPrivateContractDeployedReceipt validPrivateContractDeployed(
      final String contractAddress, final String senderAddress) {
    return new ExpectValidPrivateContractDeployedReceipt(contractAddress, senderAddress);
  }

  //  public ExpectNoPrivateContractContractDeployedReceipt noPrivateContractDeployed() {
  //    return new ExpectNoPrivateContractContractDeployedReceipt(privacyConditions,
  // privacyTransactions);
  //  }
  //
  //  public ExpectValidPrivateContractContractEventsEmitted validEventReturned(final String
  // eventValue) {
  //    return new ExpectValidPrivateContractContractEventsEmitted(eventValue, privacyConditions,
  // privacyTransactions);
  //  }
  //
  //  public ExpectNoValidPrivateContractContractEventsEmitted noValidEventReturned() {
  //    return new ExpectNoValidPrivateContractContractEventsEmitted(privacyConditions,
  // privacyTransactions);
  //  }
  //
  //  public ExpectValidPrivateContractContractValuesReturned validOutputReturned(final String
  // returnValue) {
  //    return new ExpectValidPrivateContractContractValuesReturned(returnValue, privacyConditions,
  // privacyTransactions);
  //  }
  //
  //  public ExpectNoValidPrivateContractContractValuesReturned noValidOutputReturned() {
  //    return new ExpectNoValidPrivateContractContractValuesReturned(privacyConditions,
  // privacyTransactions);
  //  }

}
