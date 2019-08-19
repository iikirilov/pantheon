/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.account;

import java.util.function.Supplier;

/** Supplier of known funded accounts defined in dev.json */
public class PrivacyAccountSupplier implements Supplier<PrivacyAccount> {

  public static final PrivacyAccount ALICE = PrivacyAccount.create("key", "orion_key_0.pub");
  public static final PrivacyAccount BOB = PrivacyAccount.create("key1", "orion_key_1.pub");
  public static final PrivacyAccount CHARLIE = PrivacyAccount.create("key2", "orion_key_2.pub");

  public static int counter = -1;

  public PrivacyAccountSupplier() {}

  @Override
  public PrivacyAccount get() {
    counter++;
    switch (counter) {
      case 0:
        return ALICE;
      case 1:
        return BOB;
      case 2:
        return CHARLIE;
      default:
        throw new RuntimeException("The are only 3 know privacy accounts");
    }
  }

  public void reset() {
    counter = -1;
  }
}
