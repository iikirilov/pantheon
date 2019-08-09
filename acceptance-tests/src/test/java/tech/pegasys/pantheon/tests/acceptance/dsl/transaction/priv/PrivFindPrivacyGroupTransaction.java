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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.priv;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.NodeRequests;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.io.IOException;
import java.util.List;

import org.web3j.protocol.pantheon.response.privacy.PrivFindPrivacyGroup;
import org.web3j.protocol.pantheon.response.privacy.PrivacyGroup;
import org.web3j.utils.Base64String;

public class PrivFindPrivacyGroupTransaction implements Transaction<List<PrivacyGroup>> {

  private final List<Base64String> addresses;

  public PrivFindPrivacyGroupTransaction(final List<String> addresses) {

    this.addresses = Base64String.wrapList(addresses);
  }

  @Override
  public List<PrivacyGroup> execute(final NodeRequests node) {
    try {
      PrivFindPrivacyGroup result =
          node.privacy().getPantheonClient().privFindPrivacyGroup(addresses).send();
      assertThat(result).isNotNull();
      return result.getResult();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
