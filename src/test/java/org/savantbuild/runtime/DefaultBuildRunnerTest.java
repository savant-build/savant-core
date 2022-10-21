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
package org.savantbuild.runtime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.dep.PathTools;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.groovy.GroovyBuildFileParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Tests the build runner.
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildRunnerTest extends BaseUnitTest {
  @Test
  public void javaProject() throws Exception {
    PathTools.prune(projectDir.resolve("test-project/build"));
    Files.createDirectories(projectDir.resolve("test-project/build"));

    BuildRunner runner = new DefaultBuildRunner(output, new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder()), new DefaultProjectRunner(output));
    runner.run(projectDir.resolve("test-project/build.savant"), new RuntimeConfiguration(false, "write"));
    assertEquals(new String(Files.readAllBytes(projectDir.resolve("test-project/build/test-file.txt")), StandardCharsets.UTF_8), "File contents");

    runner.run(projectDir.resolve("test-project/build.savant"), new RuntimeConfiguration(true, "delete"));
    assertFalse(Files.isDirectory(projectDir.resolve("test-project/build")));
  }

  @Test
  public void javaProjectWithLicenses() throws Exception {
    PathTools.prune(projectDir.resolve("test-project-licenses/build"));
    Files.createDirectories(projectDir.resolve("test-project-licenses/build"));

    BuildRunner runner = new DefaultBuildRunner(output, new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder()), new DefaultProjectRunner(output));
    runner.run(projectDir.resolve("test-project-licenses/build.savant"), new RuntimeConfiguration(false, "write"));
    assertEquals(new String(Files.readAllBytes(projectDir.resolve("test-project-licenses/build/test-file.txt")), StandardCharsets.UTF_8), "File contents");

    runner.run(projectDir.resolve("test-project-licenses/build.savant"), new RuntimeConfiguration(true, "delete"));
    assertFalse(Files.isDirectory(projectDir.resolve("test-project-licenses/build")));
  }
}
