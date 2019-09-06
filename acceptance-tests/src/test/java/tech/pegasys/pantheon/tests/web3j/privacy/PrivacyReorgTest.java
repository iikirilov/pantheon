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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.controller.KeyPairUtil.loadKeyPair;
import static tech.pegasys.pantheon.crypto.Hash.keccak256;
import static tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent.EventType.CHAIN_REORG;

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.controller.MainnetPantheonControllerBuilder;
import tech.pegasys.pantheon.controller.PantheonController;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.chain.BlockAddedObserver;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.ethereum.core.MiningParametersTestBuilder;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.eth.EthProtocolConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.SyncMode;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPoolConfiguration;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;
import tech.pegasys.pantheon.metrics.ObservableMetricsSystem;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.RocksDbConfiguration;
import tech.pegasys.pantheon.testutil.TestClock;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrivacyReorgTest {
  @ClassRule public static final TemporaryFolder FOLDER = new TemporaryFolder();

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE =
      new WireMockRule(wireMockConfig().dynamicPort());

  private static final BlockDataGenerator GENERATOR = new BlockDataGenerator();

  private static final String TRANSACTION_KEY = "93Ky7lXwFkMc7+ckoFgUMku5bpr9tz4zhmWmk9RlNng=";
  private static final SECP256K1.KeyPair KEY_PAIR =
      SECP256K1.KeyPair.create(
          SECP256K1.PrivateKey.create(
              new BigInteger(
                  "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63", 16)));

  private static final Transaction PUBLIC_TRANSACTION =
      Transaction.builder()
          .nonce(0)
          .gasPrice(Wei.of(1000))
          .gasLimit(3000000)
          .to(Address.DEFAULT_PRIVACY)
          .value(Wei.ZERO)
          .payload(BytesValues.fromBase64(TRANSACTION_KEY))
          .sender(Address.fromHexString("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73"))
          .chainId(BigInteger.valueOf(2018))
          .signAndBuild(KEY_PAIR);

  @Test
  public void privacyReorgTest() throws IOException {
    final Path dataDir = FOLDER.newFolder().toPath();
    final Path dbPath = dataDir.resolve("database");
    final SECP256K1.KeyPair nodeKeys = loadKeyPair(dbPath);
    final SynchronizerConfiguration syncConfigAhead =
        SynchronizerConfiguration.builder().syncMode(SyncMode.FULL).build();
    final ObservableMetricsSystem noOpMetricsSystem = new NoOpMetricsSystem();
    final BigInteger networkId = BigInteger.valueOf(2929);

    final PrivacyParameters privacyParameters =
        new PrivacyParameters.Builder()
            .setEnclaveUrl(URI.create(WIRE_MOCK_RULE.baseUrl()))
            .setEnabled(true)
            .setDataDir(dataDir)
            .build();

    stubFor(
        post("/receive")
            .willReturn(
                aResponse()
                    .withBody(
                        "{\"payload\":\"K1FKS2dJSUQ2SU10eHNDQWdMa0J5MkNBWUVCU05JQVZZUUFRVjJBQWdQMWJVR0FBZ0ZSZ0FXQ2dZQUlLQXhrV014ZVFWV0VCbVlCaEFESmdBRGxnQVBQK1lJQmdRRkpnQkRZUVlRQldWMlAvLy8vL2ZBRUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFZQUExQkJaalA2VHlSWUVVWVFCYlY0QmpZRmMySFJSaEFJSlhnR05uNUFUT0ZHRUFybGRiWUFDQS9WczBnQlZoQUdkWFlBQ0EvVnRRWVFCd1lRRHNWbHRnUUlCUmtZSlNVWkNCa0FOZ0lBR1E4MXMwZ0JWaEFJNVhZQUNBL1Z0UVlRQ3NZQVNBTmdOZ0lJRVFGV0VBcFZkZ0FJRDlXMUExWVFEeVZsc0FXelNBRldFQXVsZGdBSUQ5VzFCaEFNTmhBVkZXVzJCQWdGRnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLytRa2hhQ1VsR1FnWkFEWUNBQmtQTmJZQUpVa0ZaYllFQ0FVVE9CVW1BZ2dRR0RrRktCVVgvSjJ5Q3Q3Y2JQSzEwbEpTc1FHckErRWtrQ3B6L0xFcmRUODlHcW90ajU5WktSZ1pBRGtKRUJrS0ZnQWxWZ0FZQlVjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vR1JZekY1QlZWbHRnQVZSei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdrRmIrb1dWaWVucHlNRmdneC9jcHl5VGdYQ0lmV3FrVGNBZVRtVVpXOGpQK0xPTzUvWnBRWHFGK2pZb0FLWUlQNTZEYVQ1em9NSTkxSm5XeWxDYUczSGUra1Z4SmFaNHNDS3JIdWNja2F1MFRscUJVZXRjanR5djA0TFJabUNCVHlQVkRicTdMN1FvRUhDS29CRnpEQkFnd2phQURWcFcwekVzSlFlWUZVZGVoblBNR0E5dGIvQ1BsckVPbGIxZnlYM1ZJYXNDS2NtVnpkSEpwWTNSbFpBPT0=\",\"privacyGroupId\":\"8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o=\"}")));

    final PantheonController<Void> controller =
        new MainnetPantheonControllerBuilder()
            .genesisConfigFile(GenesisConfigFile.development())
            .synchronizerConfiguration(syncConfigAhead)
            .ethProtocolConfiguration(EthProtocolConfiguration.defaultConfig())
            .dataDirectory(dataDir)
            .networkId(networkId)
            .miningParameters(new MiningParametersTestBuilder().enabled(false).build())
            .nodeKeys(nodeKeys)
            .metricsSystem(noOpMetricsSystem)
            .privacyParameters(privacyParameters)
            .clock(TestClock.fixed())
            .transactionPoolConfiguration(TransactionPoolConfiguration.builder().build())
            .storageProvider(createKeyValueStorageProvider(dbPath))
            .build();

    final BlockAddedObserver privacyBlockAddedObserver = (event, blockchain) -> {
      System.out.println(event.getEventType());
      if (event.getEventType() == CHAIN_REORG) {
        // FIXME need to reverse this list in handleChainReorg
        event.getRemovedTransactions().stream().filter(t -> t.getTo().isPresent()).filter(t -> t.getTo().get().equals(Address.DEFAULT_PRIVACY));
        event.getAddedTransactions();
      }
    };

    controller.getProtocolContext().getBlockchain().observeBlockAdded(privacyBlockAddedObserver);

    //make sure the blockchain and private states are empty

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(0);

    assertThat(privacyParameters.getPrivateStateStorage().getPrivateAccountState(BytesValues.fromBase64("8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o="))).isNotPresent();

    // add block with private transaction

    final Block forkBlock =
        buildBlock(
            controller.getProtocolContext().getBlockchain().getGenesisBlock(),
            "0xc8267b3f9ed36df3ff8adb51a6d030716f23eeb50270e7fce8d9822ffa7f0461",
            "0x6e71c501e5c7ea61c81f787c7c8512c95efffba9294eaa6be8a587773f95d825",
            LogsBloomFilter.empty().getHexString(),
                23176,
            PUBLIC_TRANSACTION);

    addBlock(controller.getProtocolSchedule(), controller.getProtocolContext(), forkBlock);

    // assert that the block has been added and private state has changed

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(1);

    assertThat(privacyParameters.getPrivateStateStorage().getPrivateAccountState(BytesValues.fromBase64("8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o="))).isPresent();
    assertThat(privacyParameters.getPrivateStateStorage().getPrivateAccountState(BytesValues.fromBase64("8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o=")).get()).isEqualTo(Hash.fromHexString("0x2121b68f1333e93bae8cd717a3ca68c9d7e7003f6b288c36dfc59b0f87be9590"));

    // rewind block to simulate reorg

    final boolean didRewind = controller.getProtocolContext().getBlockchain().rewindToBlock(0);

    // assert rewind is complete

    assertThat(didRewind).isTrue();

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(0);

    // assertThat(privacyParameters.getPrivateStateStorage().getPrivateAccountState(BytesValues.fromBase64("8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o="))).isPresent();
    // assertThat(privacyParameters.getPrivateStateStorage().getPrivateAccountState(BytesValues.fromBase64("8lDVI66RZHIrBsolz6Kn88Rd+WsJ4hUjb4hsh29xW/o=")).get()).isEqualTo(Hash.wrap(keccak256(RLP.NULL)));

    // add block without private transaction

    final Block newBlock =
            buildBlock(
                    controller.getProtocolContext().getBlockchain().getGenesisBlock(),
                    "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                    "0x044372c79fd9eb58d6ae3ad89877eaaba02807efa8a29a58c22f46ce0b3e606d",
                    LogsBloomFilter.empty().getHexString(),
                    0);

    addBlock(controller.getProtocolSchedule(), controller.getProtocolContext(), newBlock);

    try {
      TimeUnit.SECONDS.sleep(10);
    } catch (InterruptedException e) {
      //
    }

    // FIXME: Might need this in the future

    //    final String listenHost = InetAddress.getLoopbackAddress().getHostAddress();
    //    final JsonRpcConfiguration aheadJsonRpcConfiguration = jsonRpcConfiguration();
    //    final GraphQLConfiguration aheadGraphQLConfiguration = graphQLConfiguration();
    //    final WebSocketConfiguration aheadWebSocketConfiguration = wsRpcConfiguration();
    //    final MetricsConfiguration aheadMetricsConfiguration = metricsConfiguration();
    //    final RunnerBuilder runnerBuilder =
    //            new RunnerBuilder()
    //                    .vertx(Vertx.vertx())
    //                    .discovery(true)
    //                    .p2pAdvertisedHost(listenHost)
    //                    .p2pListenPort(0)
    //                    .maxPeers(3)
    //                    .metricsSystem(noOpMetricsSystem)
    //                    .staticNodes(emptySet());
    //
    //    final Runner runnerAhead =
    //            runnerBuilder
    //                    .pantheonController(controllerAhead)
    //                    .ethNetworkConfig(EthNetworkConfig.getNetworkConfig(DEV))
    //                    .jsonRpcConfiguration(aheadJsonRpcConfiguration)
    //                    .graphQLConfiguration(aheadGraphQLConfiguration)
    //                    .webSocketConfiguration(aheadWebSocketConfiguration)
    //                    .metricsConfiguration(aheadMetricsConfiguration)
    //                    .dataDir(dbAhead)
    //                    .build();
    //
    //      runnerAhead.start();
    //

  }

  private Block buildBlock(
      final Block previous,
      final String receiptsRoot,
      final String stateRoot,
      final String logsBloom,
      final long gasUsed,
      final Transaction... tx) {
    final BlockDataGenerator.BlockOptions options =
        GENERATOR
            .nextBlockOptions(previous)
            .setDifficulty(previous.getHeader().getDifficulty())
            .addTransaction(tx)
            .addOmmer()
            .setReceiptsRoot(Hash.fromHexString(receiptsRoot))
            .setStateRoot(Hash.fromHexString(stateRoot))
            .setGasUsed(gasUsed)
            .setLogsBloomFilter(LogsBloomFilter.fromHexString(logsBloom));

    return GENERATOR.block(options);
  }

  private StorageProvider createKeyValueStorageProvider(final Path dbAhead) throws IOException {
    return RocksDbStorageProvider.create(
        RocksDbConfiguration.builder().databaseDir(dbAhead).build(), new NoOpMetricsSystem());
  }

  private static void addBlock(
      final ProtocolSchedule<Void> protocolSchedule,
      final ProtocolContext<Void> protocolContext,
      final Block block) {
    final ProtocolSpec<Void> protocolSpec =
        protocolSchedule.getByBlockNumber(block.getHeader().getNumber());
    final BlockImporter<Void> blockImporter = protocolSpec.getBlockImporter();
    final boolean result =
        blockImporter.importBlock(protocolContext, block, HeaderValidationMode.NONE);
    if (!result) {
      throw new IllegalStateException("Unable to import block " + block.getHeader().getNumber());
    }
  }
}
