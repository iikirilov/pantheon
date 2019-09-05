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
package tech.pegasys.pantheon;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.controller.KeyPairUtil.loadKeyPair;

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.controller.MainnetPantheonControllerBuilder;
import tech.pegasys.pantheon.controller.PantheonController;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Account;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.InMemoryStorageProvider;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.ethereum.core.MiningParametersTestBuilder;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.eth.EthProtocolConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.SyncMode;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.transactions.TransactionPoolConfiguration;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.PrecompiledContract;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;
import tech.pegasys.pantheon.metrics.ObservableMetricsSystem;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.RocksDbConfiguration;
import tech.pegasys.pantheon.testutil.TestClock;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrivacyTest {

  private static final Integer ADDRESS = 9;
  @Rule public final TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void privacyReorgTest() throws IOException {
    final Path dataDir = folder.newFolder().toPath();
    final Path dbPath = dataDir.resolve("database");
    final KeyPair nodeKeys = loadKeyPair(dbPath);
    final SynchronizerConfiguration syncConfigAhead =
        SynchronizerConfiguration.builder().syncMode(SyncMode.FULL).build();
    final ObservableMetricsSystem noOpMetricsSystem = new NoOpMetricsSystem();
    final BigInteger networkId = BigInteger.valueOf(2929);

    final PrivacyParameters privacyParameters =
        new PrivacyParameters.Builder()
            .setPrivacyAddress(ADDRESS)
            .setEnabled(true)
            .setDataDir(dataDir)
            .build();

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

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(0);

    addBlock(controller.getProtocolSchedule(), controller.getProtocolContext());

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(1);

    controller.getProtocolContext().getBlockchain().rewindToBlock(0);

    assertThat(controller.getProtocolContext().getBlockchain().getChainHeadBlockNumber())
        .isEqualTo(0);

    assertThat(
        privacyParameters
            .getPrivateStateStorage()
            .getPrivateAccountState(BytesValues.fromBase64("")));

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

  private StorageProvider createKeyValueStorageProvider(final Path dbAhead) throws IOException {
    return RocksDbStorageProvider.create(
        RocksDbConfiguration.builder().databaseDir(dbAhead).build(), new NoOpMetricsSystem());
  }

  private static void addBlock(
      final ProtocolSchedule<Void> protocolSchedule, final ProtocolContext<Void> protocolContext) {
    final BlockDataGenerator generator = new BlockDataGenerator();
    final Block genesis = protocolContext.getBlockchain().getGenesisBlock();

    final BlockDataGenerator.BlockOptions options =
        generator
            .nextBlockOptions(protocolContext.getBlockchain().getGenesisBlock())
            .setDifficulty(genesis.getHeader().getDifficulty())
            .addTransaction()
            .addOmmer()
            .setReceiptsRoot(
                Hash.fromHexString(
                    "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"))
            .setGasUsed(0)
            .setLogsBloomFilter(LogsBloomFilter.empty())
            .setStateRoot(
                Hash.fromHexString(
                    "0x990cf61fc03a06e07357749167e981dcb9e482ad0cf8bcc644e46764c52e1c9e"));

    final Block block = generator.block(options);

    final ProtocolSpec<Void> protocolSpec =
        protocolSchedule.getByBlockNumber(block.getHeader().getNumber());
    final BlockImporter<Void> blockImporter = protocolSpec.getBlockImporter();
    final boolean result =
        blockImporter.importBlock(protocolContext, block, HeaderValidationMode.NONE);
    if (!result) {
      throw new IllegalStateException("Unable to import block " + block.getHeader().getNumber());
    }
  }

  @Test
  public void privacyPrecompiled() throws IOException {
    final Path dataDir = folder.newFolder().toPath();
    final PrivacyParameters privacyParameters =
        new PrivacyParameters.Builder()
            .setPrivacyAddress(ADDRESS)
            .setEnabled(true)
            .setDataDir(dataDir)
            .build();

    final PantheonController<?> pantheonController =
        new PantheonController.Builder()
            .fromGenesisConfig(GenesisConfigFile.mainnet())
            .synchronizerConfiguration(SynchronizerConfiguration.builder().build())
            .ethProtocolConfiguration(EthProtocolConfiguration.defaultConfig())
            .storageProvider(new InMemoryStorageProvider())
            .networkId(BigInteger.ONE)
            .miningParameters(new MiningParametersTestBuilder().enabled(false).build())
            .nodeKeys(KeyPair.generate())
            .metricsSystem(new NoOpMetricsSystem())
            .dataDirectory(dataDir)
            .clock(TestClock.fixed())
            .privacyParameters(privacyParameters)
            .transactionPoolConfiguration(TransactionPoolConfiguration.builder().build())
            .build();

    final Address privacyContractAddress = Address.privacyPrecompiled(ADDRESS);
    final PrecompiledContract precompiledContract =
        pantheonController
            .getProtocolSchedule()
            .getByBlockNumber(1)
            .getPrecompileContractRegistry()
            .get(privacyContractAddress, Account.DEFAULT_VERSION);
    assertThat(precompiledContract.getName()).isEqualTo("Privacy");
  }
}
