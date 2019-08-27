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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.privacy.methods.priv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.enclave.Enclave;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.privacy.parameters.CreatePrivacyGroupParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

import org.junit.Test;

public class PrivCreatePrivacyGroupTest {

  private final Enclave enclave = mock(Enclave.class);
  private final PrivacyParameters privacyParameters = mock(PrivacyParameters.class);
  private final JsonRpcParameter parameters = new JsonRpcParameter();

  private final String from = "first participant";
  private final String name = "testName";
  private final String description = "testDesc";
  private final String[] addresses =
      new String[] {
        from,
        "second participant"
      };

  @Test
  public void verifyCreatePrivacyGroup() throws Exception {
    final String expected = "a wonderful group";
    final PrivacyGroup privacyGroup =
        new PrivacyGroup(expected, PrivacyGroup.Type.PANTHEON, name, description, addresses);
    when(enclave.createPrivacyGroup(any())).thenReturn(privacyGroup);
    when(privacyParameters.getEnclavePublicKey()).thenReturn(from);

    final PrivCreatePrivacyGroup privCreatePrivacyGroup =
        new PrivCreatePrivacyGroup(enclave, privacyParameters, parameters);

    final CreatePrivacyGroupParameter param =
        new CreatePrivacyGroupParameter(addresses, name, description);

    final Object[] params = new Object[] {param};

    final JsonRpcRequest request = new JsonRpcRequest("1", "priv_createPrivacyGroup", params);

    final JsonRpcSuccessResponse response =
        (JsonRpcSuccessResponse) privCreatePrivacyGroup.response(request);

    final String result = (String) response.getResult();

    assertThat(result).isEqualTo(expected);
  }
}
