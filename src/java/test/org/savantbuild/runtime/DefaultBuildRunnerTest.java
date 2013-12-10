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

import org.savantbuild.BaseTest;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.groovy.GroovyBuildFileParser;
import org.testng.annotations.Test;

import java.nio.file.Files;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the build runner.
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildRunnerTest extends BaseTest {
  @Test
  public void javaProject() {
    BuildRunner runner = new DefaultBuildRunner(new GroovyBuildFileParser(new DefaultTargetGraphBuilder()), new DefaultProjectRunner());
    runner.run(projectDir.resolve("test-project/build.savant"), asList("clean"));
    assertFalse(Files.isDirectory(projectDir.resolve("test-project/build")));

    runner.run(projectDir.resolve("test-project/build.savant"), asList("compile"));
    assertTrue(Files.isRegularFile(projectDir.resolve("test-project/build/classes/main/MyClass.class")));
  }
}
