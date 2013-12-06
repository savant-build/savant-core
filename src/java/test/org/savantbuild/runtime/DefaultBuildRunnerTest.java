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
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.TargetGraphBuilder;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the default build runner.
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildRunnerTest extends BaseTest {
  public TargetGraphBuilder targetGraphBuilder = new DefaultTargetGraphBuilder();

  @Test
  public void runNoDependencies() {
    Runnable cleanRunner = createStrictMock(Runnable.class);
    cleanRunner.run();
    replay(cleanRunner);

    Project project = new Project();
    project.targets.put("clean", new Target("clean", "Cleans the project", cleanRunner));
    project.targetGraph = targetGraphBuilder.build(project);

    BuildRunner runner = new DefaultBuildRunner();
    runner.run(project, asList("clean"));

    verify(cleanRunner);
  }

  @Test
  public void runMissingTarget() {
    Runnable cleanRunner = createStrictMock(Runnable.class);
    replay(cleanRunner);

    Project project = new Project();
    project.targets.put("clean", new Target("clean", "Cleans the project", cleanRunner));
    project.targetGraph = targetGraphBuilder.build(project);

    BuildRunner runner = new DefaultBuildRunner();
    try {
      runner.run(project, asList("clear")); // Simulates a user typo
      fail("Should have failed");
    } catch (BuildRunException e) {
      // Expected
      assertTrue(e.getMessage().contains("clear"));
    }

    verify(cleanRunner);
  }
}
