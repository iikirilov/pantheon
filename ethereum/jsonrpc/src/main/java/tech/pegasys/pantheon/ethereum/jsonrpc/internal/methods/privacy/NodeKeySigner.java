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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.privacy;

import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPool;
import tech.pegasys.pantheon.ethereum.jsonrpc.JsonRpcErrorConverter;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import com.google.common.base.Charsets;

public class NodeKeySigner implements Signer {

  private final SECP256K1.KeyPair nodeKeyPair;
  private TransactionPool transactionPool;
  private final Address privacyPrecompileAddress;

  public NodeKeySigner(
      final PrivacyParameters privacyParameters, final TransactionPool transactionPool) {
    this(
        privacyParameters.getSigningKeyPair(),
        transactionPool,
        Address.privacyPrecompiled(privacyParameters.getPrivacyAddress()));
  }

  public NodeKeySigner(
      final SECP256K1.KeyPair nodeKeyPair,
      final TransactionPool transactionPool,
      final Address privacyPrecompileAddress) {
    this.nodeKeyPair = nodeKeyPair;
    this.transactionPool = transactionPool;
    this.privacyPrecompileAddress = privacyPrecompileAddress;
  }

  @Override
  public Object signAndSend(
      final String enclaveKey, final PrivateTransaction privateTransaction, final long nonce) {
    final Transaction privacyMarkerTransaction =
        createPrivacyMarkerTransaction(enclaveKey, privateTransaction, nonce);
    return transactionPool
        .addLocalTransaction(privacyMarkerTransaction)
        .either(
            () -> privacyMarkerTransaction.hash().toString(),
            JsonRpcErrorConverter::convertTransactionInvalidReason);
  }

  private Transaction createPrivacyMarkerTransaction(
      final String transactionEnclaveKey,
      final PrivateTransaction privateTransaction,
      final Long nonce) {

    return Transaction.builder()
        .nonce(nonce)
        .gasPrice(privateTransaction.getGasPrice())
        .gasLimit(privateTransaction.getGasLimit())
        .to(privacyPrecompileAddress)
        .value(privateTransaction.getValue())
        .payload(BytesValue.wrap(transactionEnclaveKey.getBytes(Charsets.UTF_8)))
        .sender(privateTransaction.getSender())
        .signAndBuild(nodeKeyPair);
  }
}
