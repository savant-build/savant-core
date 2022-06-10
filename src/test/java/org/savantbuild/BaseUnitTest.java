/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.URLProcess;
import org.savantbuild.output.Output;
import org.savantbuild.output.SystemOutOutput;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.sun.net.httpserver.HttpServer;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * Base class for unit tests.
 *
 * @author Brian Pontarelli
 */
@Test(groups = "unit")
public abstract class BaseUnitTest {
  public static Output output = new SystemOutOutput(false);

  public static Path projectDir;

  public static Path cache;

  public static Workflow workflow;

  @BeforeSuite
  public static void setup() {
    projectDir = Paths.get("");
    if (!Files.isRegularFile(projectDir.resolve("LICENSE"))) {
      projectDir = projectDir.resolve("../savant-core");
    }

    cache = projectDir.resolve("build/test/cache");

    workflow = new Workflow(
        new FetchWorkflow(output, new CacheProcess(output, cache.toString()), new URLProcess(output, "http://localhost:7042/test-deps/savant", null, null)),
        new PublishWorkflow(new CacheProcess(output, cache.toString()))
    );
  }

  /**
   * Creates a file server that will accept HTTP connections on localhost:7042 and return the bytes of the file in the
   * request starting from the project directory.
   *
   * @param username (Optional) The username to verify was sent to the server in the Authentication header. Leave blank
   *                 to not check.
   * @param password (Optional) The password to verify was sent to the server in the Authentication header. Leave blank
   *                 to not check.
   * @return The server.
   * @throws IOException If the server could not be created.
   */
  protected HttpServer makeFileServer(String username, String password) throws IOException {
    InetSocketAddress localhost = new InetSocketAddress(7042);
    HttpServer server = HttpServer.create(localhost, 0);
    server.createContext("/", (httpExchange) -> {
      if (username != null) {
        assertEquals(httpExchange.getRequestHeaders().get("Authorization").get(0), "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
      }

      // Verify a GET request
      if (!httpExchange.getRequestMethod().equals("GET")) {
        fail("Should have been a GET request");
      }

      // Close the input stream because we don't need to read anything
      httpExchange.getRequestBody().close();

      String path = httpExchange.getRequestURI().getPath();
      Path file = projectDir.resolve(path.substring(1));
      if (Files.isRegularFile(file)) {
        httpExchange.sendResponseHeaders(200, Files.size(file));
        byte[] bytes = Files.readAllBytes(file);
        httpExchange.getResponseBody().write(bytes);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
      } else {
        httpExchange.sendResponseHeaders(404, 0);
      }
    });

    server.start();

    return server;
  }
}
