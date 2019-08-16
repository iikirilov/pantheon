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

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.consensys.cava.io.file.Files.copyResource;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

public class OrionTestHarnessFactory {

  //  private static final Logger LOG = LogManager.getLogger();
  //  protected static final String HOST = "127.0.0.1";

  public static OrionTestHarness create(
      final Path tempDir,
      final Ed25519PublicKeyParameters pubKey,
      final String pubKeyPath,
      final Ed25519PrivateKeyParameters privKey,
      final String privKeyPath,
      final String... othernodes)
      throws IOException {
    return create(
        tempDir, pubKeyPath, privKeyPath, pubKey.getEncoded(), privKey.getEncoded(), othernodes);
  }

  public static OrionTestHarness create(
      final Path tempDir,
      final PublicKey pubKey,
      final String pubKeyPath,
      final PrivateKey privKey,
      final String privKeyPath,
      final String... othernodes)
      throws IOException {
    return create(
        tempDir, pubKeyPath, privKeyPath, pubKey.getEncoded(), privKey.getEncoded(), othernodes);
  }

  private static OrionTestHarness create(
      final Path tempDir,
      final String pubKeyPath,
      final String privKeyPath,
      final byte[] encodedPubKey,
      final byte[] encodedPrivKey,
      final String[] othernodes)
      throws IOException {
    final Path pubKeyFile = tempDir.resolve(pubKeyPath);
    final CharSink pubKeySink = Files.asCharSink(pubKeyFile.toFile(), UTF_8);
    pubKeySink.write(Base64.getEncoder().encodeToString(encodedPubKey));

    final Path privKeyFile = tempDir.resolve(privKeyPath);
    final CharSink privKeySink = Files.asCharSink(privKeyFile.toFile(), UTF_8);
    privKeySink.write(Base64.getEncoder().encodeToString(encodedPrivKey));
    return create(tempDir, pubKeyFile, privKeyFile, Arrays.asList(othernodes));
  }

  public static OrionTestHarness create(final OrionFactoryKeyConfiguration orionConfig)
      throws IOException {
    return create(
        java.nio.file.Files.createTempDirectory("acctest-orion"),
        orionConfig.getPubKeyPath(),
        orionConfig.getPrivKeyPath(),
        Collections.emptyList());
  }

  public static OrionTestHarness create(
      final Path tempDir,
      final String pubKeyPath,
      final String privKeyPath,
      final List<String> othernodes)
      throws IOException {
    Path key1pub = copyResource(pubKeyPath, tempDir.resolve(pubKeyPath));
    Path key1key = copyResource(privKeyPath, tempDir.resolve(privKeyPath));

    return create(tempDir, key1pub, key1key, othernodes);
  }

  public static OrionTestHarness create(
      final Path tempDir, final Path key1pub, final Path key1key, final List<String> othernodes) {

    return new OrionTestHarness(new OrionConfiguration(key1pub, key1key, tempDir, othernodes));
  }
}
