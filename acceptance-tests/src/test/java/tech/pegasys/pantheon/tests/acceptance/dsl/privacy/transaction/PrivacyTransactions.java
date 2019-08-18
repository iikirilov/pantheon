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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.transaction;

import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eea.EeaGetTransactionReceiptTransaction;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.priv.PrivGetTransactionCountTransaction;

public class PrivacyTransactions {

  public EeaGetTransactionReceiptTransaction getTransactionReceipt(final String transactionHash) {
    return new EeaGetTransactionReceiptTransaction(transactionHash);
  }

  public PrivGetTransactionCountTransaction getTransactionCount(
      final String address, final String privacyGroupId) {
    return new PrivGetTransactionCountTransaction(address, privacyGroupId);
  }

  public EeaGetTransactionReceiptTransaction getPrivateTransactionReceipt(
      final String transactionHash) {
    return new EeaGetTransactionReceiptTransaction(transactionHash);
  }

  public GetAllPrivacyMarkerTransactionHashes getPrivateTransactions() {
    return new GetAllPrivacyMarkerTransactionHashes();
  }

  public CreatePrivacyGroup createPrivacyGroup(final PrivacyNode... nodes) {
    return new CreatePrivacyGroup(nodes);
  }
}
