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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.contract;

import java.util.Arrays;
import java.util.List;

import org.web3j.tx.Contract;

public class PrivateContractTransactions {

  public <T extends Contract>
      DeployPrivateSmartContractWithPrivacyGroupIdTransaction<T>
          createSmartContractWithPrivacyGroupId(
              final Class<T> clazz,
              final String transactionSigningKey,
              final long chainId,
              final String privateFrom,
              final String privacyGroupId) {
    return new DeployPrivateSmartContractWithPrivacyGroupIdTransaction<>(
        clazz, transactionSigningKey, chainId, privateFrom, privacyGroupId);
  }

  public <T extends Contract> DeployPrivateSmartContractTransaction<T> createSmartContract(
      final Class<T> clazz,
      final String transactionSigningKey,
      final long chainId,
      final String privateFrom,
      final String... privateFor) {
    return createSmartContract(
        clazz, transactionSigningKey, chainId, privateFrom, Arrays.asList(privateFor));
  }

  public <T extends Contract> DeployPrivateSmartContractTransaction<T> createSmartContract(
      final Class<T> clazz,
      final String transactionSigningKey,
      final long chainId,
      final String privateFrom,
      final List<String> privateFor) {
    return new DeployPrivateSmartContractTransaction<>(
        clazz, transactionSigningKey, chainId, privateFrom, privateFor);
  }

  public PrivateCallSmartContractFunction callSmartContract(
      final String functionName, final String contractAddress) {
    return new PrivateCallSmartContractFunction(functionName, contractAddress);
  }

  public <T extends Contract> PrivateLoadSmartContractTransaction<T> loadSmartContract(
      final String contractAddress,
      Class<T> clazz,
      String transactionSigningKey,
      long chainId,
      String privateFrom,
      String... privateFor) {
    return loadSmartContract(
        contractAddress,
        clazz,
        transactionSigningKey,
        chainId,
        privateFrom,
        Arrays.asList(privateFor));
  }

  private <T extends Contract> PrivateLoadSmartContractTransaction<T> loadSmartContract(
      final String contractAddress,
      Class<T> clazz,
      String transactionSigningKey,
      long chainId,
      String privateFrom,
      List<String> privateFor) {
    return new PrivateLoadSmartContractTransaction<>(
        contractAddress, clazz, transactionSigningKey, chainId, privateFrom, privateFor);
  }
}
