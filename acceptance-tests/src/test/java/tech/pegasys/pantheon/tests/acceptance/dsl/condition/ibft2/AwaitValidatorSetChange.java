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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.ibft2;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;
import static tech.pegasys.pantheon.tests.acceptance.dsl.transaction.clique.CliqueTransactions.LATEST;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.ibft2.Ibft2Transactions;

import java.util.List;

public class AwaitValidatorSetChange implements Condition {

  private final Ibft2Transactions ibft;
  private final List<Address> initialSigners;

  public AwaitValidatorSetChange(final List<Address> initialSigners, final Ibft2Transactions ibft) {
    this.initialSigners = initialSigners;
    this.ibft = ibft;
  }

  @Override
  public void verify(final Node node) {
    waitFor(
        60,
        () ->
            assertThat(node.execute(ibft.createGetValidators(LATEST)))
                .isNotEqualTo(initialSigners));
  }
}
