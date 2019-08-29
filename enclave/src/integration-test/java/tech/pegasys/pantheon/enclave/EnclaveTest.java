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
package tech.pegasys.pantheon.enclave;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertTrue;

import tech.pegasys.orion.testutil.OrionTestHarness;
import tech.pegasys.orion.testutil.OrionTestHarnessFactory;
import tech.pegasys.pantheon.enclave.types.CreatePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.DeletePrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.FindPrivacyGroupRequest;
import tech.pegasys.pantheon.enclave.types.PrivacyGroup;
import tech.pegasys.pantheon.enclave.types.PushToHistoryRequest;
import tech.pegasys.pantheon.enclave.types.ReceiveRequest;
import tech.pegasys.pantheon.enclave.types.ReceiveResponse;
import tech.pegasys.pantheon.enclave.types.SendRequestLegacy;
import tech.pegasys.pantheon.enclave.types.SendRequestPantheon;
import tech.pegasys.pantheon.enclave.types.SendResponse;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EnclaveTest {

  @ClassRule public static final TemporaryFolder folder = new TemporaryFolder();

  private static final String PAYLOAD = "a wonderful transaction";
  private static Enclave enclave;

  private static OrionTestHarness testHarness;

  @Before
  public void setUpOnce() throws Exception {
    folder.create();

    testHarness =
        OrionTestHarnessFactory.create(
            folder.newFolder().toPath(), "orion_key_0.pub", "orion_key_0.key");

    enclave = new Enclave(testHarness.clientUrl());
  }

  @After
  public void tearDownOnce() {
    testHarness.getOrion().stop();
  }

  @Test
  public void testUpCheck() throws IOException {
    assertTrue(enclave.upCheck());
  }

  @Test
  public void testSendAndReceive() throws Exception {
    final List<String> publicKeys = testHarness.getPublicKeys();

    final SendResponse sr =
        enclave.send(
            new SendRequestLegacy(
                PAYLOAD, publicKeys.get(0), Lists.newArrayList(publicKeys.get(0))));
    final ReceiveResponse rr = enclave.receive(new ReceiveRequest(sr.getKey(), publicKeys.get(0)));
    assertThat(rr).isNotNull();
    assertThat(new String(rr.getPayload(), UTF_8)).isEqualTo(PAYLOAD);
    assertThat(rr.getPrivacyGroupId()).isNotNull();
  }

  @Test
  public void testSendWithPrivacyGroupAndReceive() throws Exception {
    final List<String> publicKeys = testHarness.getPublicKeys();

    final CreatePrivacyGroupRequest privacyGroupRequest =
        new CreatePrivacyGroupRequest(publicKeys.toArray(new String[0]), publicKeys.get(0), "", "");

    final PrivacyGroup privacyGroup = enclave.createPrivacyGroup(privacyGroupRequest);

    final SendResponse sr =
        enclave.send(
            new SendRequestPantheon(PAYLOAD, publicKeys.get(0), privacyGroup.getPrivacyGroupId()));
    final ReceiveResponse rr = enclave.receive(new ReceiveRequest(sr.getKey(), publicKeys.get(0)));
    assertThat(rr).isNotNull();
    assertThat(new String(rr.getPayload(), UTF_8)).isEqualTo(PAYLOAD);
    assertThat(rr.getPrivacyGroupId()).isNotNull();
  }

  @Test
  public void testCreateAndDeletePrivacyGroup() throws Exception {
    final List<String> publicKeys = testHarness.getPublicKeys();
    final String name = "testName";
    final String description = "testDesc";
    final CreatePrivacyGroupRequest privacyGroupRequest =
        new CreatePrivacyGroupRequest(
            publicKeys.toArray(new String[0]), publicKeys.get(0), name, description);

    final PrivacyGroup privacyGroup = enclave.createPrivacyGroup(privacyGroupRequest);

    assertThat(privacyGroup.getPrivacyGroupId()).isNotNull();
    assertThat(privacyGroup.getName()).isEqualTo(name);
    assertThat(privacyGroup.getDescription()).isEqualTo(description);
    assertThat(privacyGroup.getType()).isEqualByComparingTo(PrivacyGroup.Type.PANTHEON);

    final String response =
        enclave.deletePrivacyGroup(
            new DeletePrivacyGroupRequest(privacyGroup.getPrivacyGroupId(), publicKeys.get(0)));

    assertThat(privacyGroup.getPrivacyGroupId()).isEqualTo(response);
  }

  @Test
  public void testCreateFindDeleteFindPrivacyGroup() throws Exception {
    List<String> publicKeys = testHarness.getPublicKeys();
    String name = "name";
    String description = "desc";
    CreatePrivacyGroupRequest privacyGroupRequest =
        new CreatePrivacyGroupRequest(
            publicKeys.toArray(new String[0]), publicKeys.get(0), name, description);

    PrivacyGroup privacyGroup = enclave.createPrivacyGroup(privacyGroupRequest);

    assertThat(privacyGroup.getPrivacyGroupId()).isNotNull();
    assertThat(privacyGroup.getName()).isEqualTo(name);
    assertThat(privacyGroup.getDescription()).isEqualTo(description);
    assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);

    FindPrivacyGroupRequest findPrivacyGroupRequest =
        new FindPrivacyGroupRequest(publicKeys.toArray(new String[0]));
    PrivacyGroup[] findprivacyGroup = enclave.findPrivacyGroup(findPrivacyGroupRequest);

    assertThat(findprivacyGroup.length).isEqualTo(1);
    assertThat(findprivacyGroup[0].getPrivacyGroupId()).isEqualTo(privacyGroup.getPrivacyGroupId());

    DeletePrivacyGroupRequest deletePrivacyGroupRequest =
        new DeletePrivacyGroupRequest(privacyGroup.getPrivacyGroupId(), publicKeys.get(0));

    String response = enclave.deletePrivacyGroup(deletePrivacyGroupRequest);

    assertThat(privacyGroup.getPrivacyGroupId()).isEqualTo(response);

    findPrivacyGroupRequest = new FindPrivacyGroupRequest(publicKeys.toArray(new String[0]));
    findprivacyGroup = enclave.findPrivacyGroup(findPrivacyGroupRequest);

    assertThat(findprivacyGroup.length).isEqualTo(0);
  }

  @Test
  public void testPushToHistory() throws Exception {
    final List<String> publicKeys = testHarness.getPublicKeys();
    final String name = "testName";
    final String description = "testDesc";
    final CreatePrivacyGroupRequest privacyGroupRequest =
        new CreatePrivacyGroupRequest(
            publicKeys.toArray(new String[0]), publicKeys.get(0), name, description);

    final PrivacyGroup privacyGroup = enclave.createPrivacyGroup(privacyGroupRequest);

    final SendResponse sendResponse =
        enclave.send(
            new SendRequestLegacy(
                PAYLOAD, publicKeys.get(0), Lists.newArrayList(publicKeys.get(0))));

    final boolean pushResponse =
        enclave.pushToHistory(
            new PushToHistoryRequest(
                privacyGroup.getPrivacyGroupId(),
                "0x35d862d86c294932cd6561ec5c061747b093a48138082eaad7175da0ea83feab",
                sendResponse.getKey()));

    assertThat(pushResponse).isNotNull();
    assertThat(pushResponse).isTrue();
  }

  @Test
  public void whenUpCheckFailsThrows() {
    final Throwable thrown = catchThrowable(() -> new Enclave(URI.create("http://null")).upCheck());
    assertThat(thrown).isInstanceOf(IOException.class);
    assertThat(thrown).hasMessageContaining("Failed to perform upcheck");
  }
}
