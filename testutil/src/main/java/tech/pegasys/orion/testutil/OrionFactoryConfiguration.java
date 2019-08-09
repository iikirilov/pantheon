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
package tech.pegasys.orion.testutil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrionFactoryConfiguration {
  private final String pubKeyPath;
  private final String privKeyPath;
  private final List<String> otherNodes;

  public OrionFactoryConfiguration(String pubKeyPath, String privKeyPath, List<String> otherNodes)
      throws IOException {
    this.pubKeyPath = pubKeyPath;
    this.privKeyPath = privKeyPath;
    this.otherNodes = otherNodes;
  }

  public OrionFactoryConfiguration(String pubKeyPath, String privKeyPath, String... otherNodes)
      throws IOException {
    this(pubKeyPath, privKeyPath, Arrays.asList(otherNodes));
  }

  public OrionFactoryConfiguration(String pubKeyPath, String privKeyPath, String otherNode)
      throws IOException {
    this(pubKeyPath, privKeyPath, Collections.singletonList(otherNode));
  }

  public String getPubKeyPath() {
    return pubKeyPath;
  }

  public String getPrivKeyPath() {
    return privKeyPath;
  }

  public List<String> getOtherNodes() {
    return otherNodes;
  }
}
