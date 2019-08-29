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

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.transaction.PrivacyTransactions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;
import org.web3j.protocol.pantheon.response.privacy.PrivacyGroup;
import org.web3j.utils.Base64String;

public class ExpectValidPrivacyGroupCreated implements PrivateCondition {
  private final PrivacyTransactions transactions;
  private final String privacyGroupId;
  private final String name;
  private final String description;
  private final List<PrivacyNode> nodes;

  public ExpectValidPrivacyGroupCreated(
      final PrivacyTransactions transactions,
      final String privacyGroupId,
      final String name,
      final String description,
      final List<PrivacyNode> nodes) {
    this.transactions = transactions;
    this.privacyGroupId = privacyGroupId;
    this.name = name;
    this.description = description;
    this.nodes = nodes;
  }

  @Override
  public void verify(final PrivacyNode node) {
    final PrivacyGroup privacyGroup =
        Awaitility.await()
            .until(
                () -> {
                  final List<PrivacyGroup> groups =
                      node.execute(transactions.findPrivacyGroup(nodes));
                  if (groups.size() > 1) {
                    assertThat(groups.size()).isEqualTo(1);
                    return groups.get(0);
                  }
                  return null;
                },
                Objects::nonNull);

    assertThat(privacyGroup.getPrivacyGroupId().toString()).isEqualTo(privacyGroupId);
    assertThat(privacyGroup.getName()).isEqualTo(name);
    assertThat(privacyGroup.getDescription()).isEqualTo(description);
    assertThat(privacyGroup.getMembers().size()).isEqualTo(nodes.size());

    final List<String> members =
        privacyGroup.getMembers().stream().map(Base64String::toString).collect(Collectors.toList());
    nodes.forEach(n -> assertThat(members).contains(n.getEnclaveKey()));
  }
}
