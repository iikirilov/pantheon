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
package tech.pegasys.ethsigner.testutil;

import static net.consensys.cava.io.file.Files.copyResource;

import tech.pegasys.ethsigner.core.RunnerBuilder;
import tech.pegasys.ethsigner.core.signing.ConfigurationChainId;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EthSignerTestHarnessFactory {

  private static final Logger LOG = LogManager.getLogger();
  protected static final String HOST = "127.0.0.1";

  //  public static EthSignerTestHarness create(
  //      final Path tempDir,
  //      final Ed25519PublicKeyParameters pubKey,
  //      final String pubKeyPath,
  //      final Ed25519PrivateKeyParameters privKey,
  //      final String privKeyPath,
  //      final String... othernodes)
  //      throws IOException {
  //    return create(
  //        tempDir, pubKeyPath, privKeyPath, pubKey.getEncoded(), privKey.getEncoded(),
  // othernodes);
  //  }
  //
  //  public static EthSignerTestHarness create(
  //      final Path tempDir,
  //      final PublicKey pubKey,
  //      final String pubKeyPath,
  //      final PrivateKey privKey,
  //      final String privKeyPath,
  //      final String... othernodes)
  //      throws IOException {
  //    return create(
  //        tempDir, pubKeyPath, privKeyPath, pubKey.getEncoded(), privKey.getEncoded(),
  // othernodes);
  //  }
  //
  //  private static EthSignerTestHarness create(
  //      final Path tempDir,
  //      final String pubKeyPath,
  //      final String privKeyPath,
  //      final byte[] encodedPubKey,
  //      final byte[] encodedPrivKey,
  //      final String[] othernodes)
  //      throws IOException {
  //    final Path pubKeyFile = tempDir.resolve(pubKeyPath);
  //    final CharSink pubKeySink = Files.asCharSink(pubKeyFile.toFile(), UTF_8);
  //    pubKeySink.write(Base64.getEncoder().encodeToString(encodedPubKey));
  //
  //    final Path privKeyFile = tempDir.resolve(privKeyPath);
  //    final CharSink privKeySink = Files.asCharSink(privKeyFile.toFile(), UTF_8);
  //    privKeySink.write(Base64.getEncoder().encodeToString(encodedPrivKey));
  //    return create(tempDir, pubKeyFile, privKeyFile, othernodes);
  //  }
  //
  //  public static EthSignerTestHarness create(
  //      final Path tempDir,
  //      final String pubKeyPath,
  //      final String privKeyPath,
  //      final String... othernodes)
  //      throws IOException {
  //    Path key1pub = copyResource(pubKeyPath, tempDir.resolve(pubKeyPath));
  //    Path key1key = copyResource(privKeyPath, tempDir.resolve(privKeyPath));
  //
  //    return create(tempDir, key1pub, key1key, othernodes);
  //  }

  public static EthSignerTestHarness create(
      final Path tempDir,
      final String keyPath,
      final Integer pantheonPort,
      final Integer ethsignerPort,
      final long chainId)
      throws IOException {

    Path keyFilePath = copyResource(keyPath, tempDir.resolve(keyPath));

    final File emptyPasswordFile = new File(tempDir.toString() + "/empty");
    try {
      //noinspection ResultOfMethodCallIgnored
      emptyPasswordFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    final EthSignerConfig config =
        new EthSignerConfig(
            Level.INFO,
            emptyPasswordFile.toPath(),
            keyFilePath,
            InetAddress.getLocalHost(),
            pantheonPort,
            Duration.ofSeconds(10),
            InetAddress.getLocalHost(),
            ethsignerPort,
            new ConfigurationChainId(chainId),
            tempDir);

    final tech.pegasys.ethsigner.core.EthSigner ethSigner =
        new tech.pegasys.ethsigner.core.EthSigner(config, new RunnerBuilder());
    ethSigner.run();

    // FIXME: Figure out how to start ethsigner synchronously.
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    LOG.info("EthSigner port: {}", config.getHttpListenPort());

    return new EthSignerTestHarness(ethSigner, config);
  }
}
