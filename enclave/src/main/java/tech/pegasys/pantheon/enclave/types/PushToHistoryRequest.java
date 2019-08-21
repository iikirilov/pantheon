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
package tech.pegasys.pantheon.enclave.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PushToHistoryRequest {

  private final String privacyGroupId;
  private final String privacyMarkerTransactionHash;
  private final String enclaveKey;

  @JsonCreator
  public PushToHistoryRequest(
      @JsonProperty("privacyGroupId") final String privacyGroupId,
      @JsonProperty("privacyMarkerTransactionHash") final String privacyMarkerTransactionHash,
      @JsonProperty("enclaveKey") final String enclaveKey) {
    this.privacyGroupId = privacyGroupId;
    this.privacyMarkerTransactionHash = privacyMarkerTransactionHash;
    this.enclaveKey = enclaveKey;
  }

  @JsonProperty("privacyGroupId")
  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  @JsonProperty("privacyMarkerTransactionHash")
  public String getPrivacyMarkerTransactionHash() {
    return privacyMarkerTransactionHash;
  }

  @JsonProperty("enclaveKey")
  public String getEnclaveKey() {
    return enclaveKey;
  }
}
