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

import java.nio.file.Files;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.io.FileTools;
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
    FileTools.prune(projectDir.resolve("test-project/build"));
    Files.createDirectories(projectDir.resolve("test-project/build"));

    BuildRunner runner = new DefaultBuildRunner(new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder()), new DefaultProjectRunner(output));
    runner.run(projectDir.resolve("test-project/build.savant"), new RuntimeConfiguration(false, "write"));
    assertEquals(new String(Files.readAllBytes(projectDir.resolve("test-project/build/test-file.txt")), "UTF-8"), "File contents\n");

    runner.run(projectDir.resolve("test-project/build.savant"), new RuntimeConfiguration(true, "delete"));
    assertFalse(Files.isDirectory(projectDir.resolve("test-project/build")));
  }
}
