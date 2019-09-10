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
package tech.pegasys.pantheon.ethereum.privacy;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Log;
import tech.pegasys.pantheon.ethereum.core.LogSeries;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.Optional;

public interface PrivateStateStorage {

  boolean isPrivateStateAvailable(Bytes32 transactionHash);

  Optional<List<Log>> getTransactionLogs(Bytes32 transactionHash);

  Optional<BytesValue> getTransactionOutput(Bytes32 transactionHash);

  Optional<Hash> getPrivacyGroupLatestRootHash(BytesValue privacyId);

  boolean isWorldStateAvailable(Bytes32 rootHash);

  PrivateStateStorage.Updater updater();

  interface Updater {

    PrivateStateStorage.Updater putTransactionLogs(Bytes32 transactionHash, LogSeries logs);

    PrivateStateStorage.Updater putTransactionOutput(Bytes32 transactionHash, BytesValue events);

    PrivateStateStorage.Updater putPrivacyGroupLatestRootHash(
        BytesValue privacyId, Hash privateStateHash);

    void commit();
  }
}
