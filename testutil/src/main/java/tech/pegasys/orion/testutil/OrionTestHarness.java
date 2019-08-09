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

import static com.google.common.io.Files.readLines;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import net.consensys.orion.cmd.Orion;
import net.consensys.orion.config.Config;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrionTestHarness {
  private static final Logger LOG = LogManager.getLogger();

  private final Orion orion;
  private final Config config;

  private boolean isRunning;

  protected static final String HOST = "127.0.0.1";

  protected OrionTestHarness(final Config config) {
    this.config = config;
    this.orion = new Orion();
  }

  public Orion getOrion() {
    return orion;
  }

  public void start() {
    if (!isRunning) {
      orion.run(System.out, System.err, config);
      isRunning = true;
      LOG.info("Orion node port: {}", orion.nodePort());
      LOG.info("Orion client port: {}", orion.clientPort());
    }
  }

  public void stop() {
    if (isRunning) {
      orion.stop();
      isRunning = false;
    }
  }

  public void close() {
    stop();
    try {
      MoreFiles.deleteRecursively(config.workDir(), RecursiveDeleteOption.ALLOW_INSECURE);
    } catch (final IOException e) {
      LOG.info("Failed to clean up temporary file: {}", config.workDir(), e);
    }
  }

  public Config getConfig() {
    return config;
  }

  public String getDefaultPublicKey() {
    return config.publicKeys().stream().map(OrionTestHarness::readFile).findFirst().orElseThrow();
  }

  public List<String> getPublicKeys() {
    return config.publicKeys().stream()
        .map(OrionTestHarness::readFile)
        .collect(Collectors.toList());
  }

  private static String readFile(final Path path) {
    try {
      return readLines(path.toFile(), Charsets.UTF_8).get(0);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  public URI clientUrl() {
    HttpUrl httpUrl =
        new HttpUrl.Builder().scheme("http").host(HOST).port(orion.clientPort()).build();

    return URI.create(httpUrl.toString());
  }

  public String nodeUrl() {
    return new HttpUrl.Builder()
        .scheme("http")
        .host(HOST)
        .port(orion.nodePort())
        .build()
        .toString();
  }
}
