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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.condition;

import org.web3j.protocol.eea.response.PrivateTransactionReceipt;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.transaction.PrivacyTransactions;

public class PrivateTransactionVerifier {

  private final PrivacyTransactions transactions;

  public PrivateTransactionVerifier(final PrivacyTransactions transactions) {
    this.transactions = transactions;
  }

  public ExpectValidPrivateTransactionReceipt validPrivateTransactionReceipt(
      final String aliceExecutionTransactionHash, final PrivateTransactionReceipt receipt) {
    return new ExpectValidPrivateTransactionReceipt(
        transactions, aliceExecutionTransactionHash, receipt);
  }

  public ExpectNoPrivateTransactionReceipt noPrivateTransactionReceipt(
      final String transactionHash) {
    return new ExpectNoPrivateTransactionReceipt(transactions, transactionHash);
  }

  //    public ExpectValidPrivateContractTransactionReceipt validPrivateTransactionReceipt() {
  //      return new ExpectValidPrivateContractTransactionReceipt(eea, transactions);
  //    }
}
