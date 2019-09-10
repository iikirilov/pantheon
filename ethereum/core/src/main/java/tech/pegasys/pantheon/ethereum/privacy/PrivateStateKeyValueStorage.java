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

import static java.nio.charset.StandardCharsets.UTF_8;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Log;
import tech.pegasys.pantheon.ethereum.core.LogSeries;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.List;
import java.util.Optional;

public class PrivateStateKeyValueStorage implements PrivateStateStorage {

  private static final BytesValue EVENTS_KEY_SUFFIX = BytesValue.of("EVENTS".getBytes(UTF_8));
  private static final BytesValue OUTPUT_KEY_SUFFIX = BytesValue.of("OUTPUT".getBytes(UTF_8));

  private final KeyValueStorage keyValueStorage;

  public PrivateStateKeyValueStorage(final KeyValueStorage keyValueStorage) {
    this.keyValueStorage = keyValueStorage;
  }

  @Override
  public boolean isPrivateStateAvailable(final Bytes32 transactionHash) {
    return get(transactionHash, EVENTS_KEY_SUFFIX).isPresent()
        || get(transactionHash, OUTPUT_KEY_SUFFIX).isPresent();
  }

  @Override
  public Optional<List<Log>> getTransactionLogs(final Bytes32 transactionHash) {
    return get(transactionHash, EVENTS_KEY_SUFFIX).map(this::rlpDecodeLog);
  }

  @Override
  public Optional<BytesValue> getTransactionOutput(final Bytes32 transactionHash) {
    return get(transactionHash, OUTPUT_KEY_SUFFIX);
  }

  @Override
  public Optional<Hash> getPrivacyGroupLatestRootHash(final BytesValue privacyId) {
    if (keyValueStorage.get(privacyId).isPresent())
      return Optional.of(
          Hash.wrap(Bytes32.wrap(keyValueStorage.get(privacyId).get().extractArray())));
    else return Optional.empty();
  }

  @Override
  public boolean isWorldStateAvailable(final Bytes32 rootHash) {
    return false;
  }

  @Override
  public PrivateStateStorage.Updater updater() {
    return new PrivateStateKeyValueStorage.Updater(keyValueStorage.startTransaction());
  }

  private Optional<BytesValue> get(final BytesValue key, final BytesValue keySuffix) {
    return keyValueStorage.get(BytesValues.concatenate(key, keySuffix));
  }

  private List<Log> rlpDecodeLog(final BytesValue bytes) {
    return RLP.input(bytes).readList(Log::readFrom);
  }

  public static class Updater implements PrivateStateStorage.Updater {
    private final KeyValueStorage.Transaction transaction;

    private Updater(final KeyValueStorage.Transaction transaction) {
      this.transaction = transaction;
    }

    @Override
    public PrivateStateStorage.Updater putTransactionLogs(
        final Bytes32 transactionHash, final LogSeries logs) {
      set(transactionHash, EVENTS_KEY_SUFFIX, RLP.encode(logs::writeTo));
      return this;
    }

    @Override
    public PrivateStateStorage.Updater putTransactionOutput(
        final Bytes32 transactionHash, final BytesValue output) {
      set(transactionHash, OUTPUT_KEY_SUFFIX, output);
      return this;
    }

    @Override
    public PrivateStateStorage.Updater putPrivacyGroupLatestRootHash(
        final BytesValue privacyId, final Hash privateStateHash) {
      transaction.put(privacyId, BytesValue.wrap(privateStateHash.extractArray()));
      return this;
    }

    private void set(final BytesValue key, final BytesValue keySuffix, final BytesValue value) {
      transaction.put(BytesValues.concatenate(key, keySuffix), value);
    }

    @Override
    public void commit() {
      transaction.commit();
    }
  }
}
