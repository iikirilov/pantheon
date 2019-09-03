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
package tech.pegasys.pantheon.tests.web3j.privacy;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyAcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNode;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.pantheon.response.privacy.PrivacyGroup;

public class PrivacyGroupAcceptanceTest extends PrivacyAcceptanceTestBase {

  private PrivacyNode alice;
  private PrivacyNode bob;
  private PrivacyNode charlie;

  @Before
  public void setUp() throws Exception {
    alice =
        privacyPantheon.createPrivateTransactionEnabledMinerNode(
            "node1", privacyAccountSupplier.get());
    bob =
        privacyPantheon.createPrivateTransactionEnabledNode("node2", privacyAccountSupplier.get());
    charlie =
        privacyPantheon.createPrivateTransactionEnabledNode("node3", privacyAccountSupplier.get());
    privacyCluster.start(alice, bob, charlie);
  }

  @Test
  public void nodeCanCreatePrivacyGroup() {
    final String privacyGroupId =
        alice.execute(
            privacyTransactions.createPrivacyGroup(
                "myGroupName", "my group description", alice, bob));

    assertThat(privacyGroupId).isNotNull();

    final List<PrivacyGroup> privacyGroups =
        alice.execute(privacyTransactions.findPrivacyGroup(alice, bob));

    assertThat(privacyGroups.size()).isEqualTo(1);
    assertThat(privacyGroups.get(0).getPrivacyGroupId().toString()).isEqualTo(privacyGroupId);
    assertThat(privacyGroups.get(0).getName()).isEqualTo("myGroupName");
    assertThat(privacyGroups.get(0).getDescription()).isEqualTo("my group description");
    assertThat(privacyGroups.get(0).getMembers().size()).isEqualTo(2);
  }

  @Test
  public void nodeCanCreatePrivacyGroupWithoutName() {
    final String privacyGroupId =
        alice.execute(
            privacyTransactions.createPrivacyGroup(null, "my group description", alice, bob));

    assertThat(privacyGroupId).isNotNull();

    final List<PrivacyGroup> privacyGroups =
        alice.execute(privacyTransactions.findPrivacyGroup(alice, bob));

    assertThat(privacyGroups.size()).isEqualTo(1);
    assertThat(privacyGroups.get(0).getPrivacyGroupId().toString()).isEqualTo(privacyGroupId);
    assertThat(privacyGroups.get(0).getName()).isEqualTo("Default Name");
    assertThat(privacyGroups.get(0).getDescription()).isEqualTo("my group description");
    assertThat(privacyGroups.get(0).getMembers().size()).isEqualTo(2);
  }

  @Test
  public void nodeCanCreatePrivacyGroupWithoutDescription() {
    final String privacyGroupId =
        alice.execute(privacyTransactions.createPrivacyGroup("myGroupName", null, alice, bob));

    assertThat(privacyGroupId).isNotNull();

    final List<PrivacyGroup> privacyGroups =
        alice.execute(privacyTransactions.findPrivacyGroup(alice, bob));

    assertThat(privacyGroups.size()).isEqualTo(1);
    assertThat(privacyGroups.get(0).getPrivacyGroupId().toString()).isEqualTo(privacyGroupId);
    assertThat(privacyGroups.get(0).getName()).isEqualTo("myGroupName");
    assertThat(privacyGroups.get(0).getDescription()).isEqualTo("Default Description");
    assertThat(privacyGroups.get(0).getMembers().size()).isEqualTo(2);
  }

  @Test
  public void nodeCanCreatePrivacyGroupWithoutOptionalParams() {
    final String privacyGroupId =
        alice.execute(privacyTransactions.createPrivacyGroup(null, null, alice, bob));

    assertThat(privacyGroupId).isNotNull();

    final List<PrivacyGroup> privacyGroups =
        alice.execute(privacyTransactions.findPrivacyGroup(alice, bob));

    assertThat(privacyGroups.size()).isEqualTo(1);
    assertThat(privacyGroups.get(0).getPrivacyGroupId().toString()).isEqualTo(privacyGroupId);
    assertThat(privacyGroups.get(0).getName()).isEqualTo("Default Name");
    assertThat(privacyGroups.get(0).getDescription()).isEqualTo("Default Description");
    assertThat(privacyGroups.get(0).getMembers().size()).isEqualTo(2);
  }
}
