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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.privacy.PrivateTransaction;
import tech.pegasys.pantheon.ethsigner.EthSignerClient;

import java.io.IOException;
import java.math.BigInteger;

public class EthSigner implements Signer {
  private EthSignerClient client;
  private Address privacyPrecompileAddress;
  private static final Logger LOG = LogManager.getLogger();


  public EthSigner(final PrivacyParameters privacyParameters) throws IOException {
    this(
        new EthSignerClient(privacyParameters.getSignerUrl()),
        Address.privacyPrecompiled(privacyParameters.getPrivacyAddress()));
  }

  public EthSigner(final EthSignerClient client, final Address privacyPrecompileAddress) {
    this.client = client;
    this.privacyPrecompileAddress = privacyPrecompileAddress;
  }

  @Override
  public Object signAndSend(
      final String enclaveKey, final PrivateTransaction privateTransaction, final long nonce) {
    try {

      LOG.info("------------------");
      LOG.info("GOT HERE");
      LOG.info("------------------");
      return client.ethSendTransaction(
          privacyPrecompileAddress.toString(),
          BigInteger.valueOf(privateTransaction.getGasLimit()),
          new BigInteger(privateTransaction.getGasPrice().toUnprefixedHexString(), 16),
          new BigInteger(privateTransaction.getValue().toUnprefixedHexString()),
          enclaveKey,
          BigInteger.valueOf(nonce));
    } catch (IOException e) {
      return e.getMessage();
    }
  }
}
