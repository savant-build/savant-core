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

import java.util.ArrayList;
import java.util.List;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.TargetGraphBuilder;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the default project runner.
 *
 * @author Brian Pontarelli
 */
public class DefaultProjectRunnerTest extends BaseUnitTest {
  public TargetGraphBuilder targetGraphBuilder = new DefaultTargetGraphBuilder();

  private List<String> calledTargets = new ArrayList<>();

  @Test
  public void runDependencies() {
    calledTargets.clear();

    Runnable compileRunner = makeRunnerMock("compile");
    Runnable copyResourcesRunner = makeRunnerMock("copyResources");
    Runnable testRunner = makeRunnerMock("test");
    Runnable intRunner = makeRunnerMock("int");
    Runnable cleanRunner = makeUncalledRunnerMock();

    Project project = new Project(null, output);
    project.targets.put("clean", new Target("clean", "Cleans the project", cleanRunner));
    project.targets.put("compile", new Target("compile", "Compiles the project", compileRunner));
    project.targets.put("copyResources", new Target("copyResources", "Copies the resources to the build dir", copyResourcesRunner));
    project.targets.put("test", new Target("test", "Tests the project", testRunner, "compile", "copyResources"));
    project.targets.put("int", new Target("int", "Integrates the project", intRunner, "test"));
    project.targetGraph = targetGraphBuilder.build(project);

    ProjectRunner runner = new DefaultProjectRunner(output);
    runner.run(project, asList("int"));

    assertEquals(calledTargets, asList("compile", "copyResources", "test", "int"));

    verify(compileRunner);
    verify(copyResourcesRunner);
    verify(testRunner);
    verify(intRunner);
    verify(cleanRunner);
  }

  @Test
  public void runMissingTarget() {
    Runnable cleanRunner = createStrictMock(Runnable.class);
    replay(cleanRunner);

    Project project = new Project(null, output);
    project.targets.put("clean", new Target("clean", "Cleans the project", cleanRunner));
    project.targetGraph = targetGraphBuilder.build(project);

    ProjectRunner runner = new DefaultProjectRunner(output);
    try {
      runner.run(project, asList("clear")); // Simulates a user typo
      fail("Should have failed");
    } catch (BuildRunException e) {
      // Expected
      assertTrue(e.getMessage().contains("clear"));
    }

    verify(cleanRunner);
  }

  @Test
  public void runNoDependencies() {
    calledTargets.clear();

    Runnable compileRunner = makeUncalledRunnerMock();
    Runnable copyResourcesRunner = makeUncalledRunnerMock();
    Runnable testRunner = makeUncalledRunnerMock();
    Runnable intRunner = makeUncalledRunnerMock();
    Runnable cleanRunner = makeRunnerMock("clean");

    Project project = new Project(null, output);
    project.targets.put("clean", new Target("clean", "Cleans the project", cleanRunner));
    project.targets.put("compile", new Target("compile", "Compiles the project", compileRunner));
    project.targets.put("copyResources", new Target("compile", "Compiles the project", copyResourcesRunner));
    project.targets.put("test", new Target("test", "Tests the project", testRunner, "compile", "copyResources"));
    project.targets.put("int", new Target("int", "Integrates the project", intRunner, "test"));
    project.targetGraph = targetGraphBuilder.build(project);

    ProjectRunner runner = new DefaultProjectRunner(output);
    runner.run(project, asList("clean"));

    assertEquals(calledTargets, asList("clean"));

    verify(compileRunner);
    verify(copyResourcesRunner);
    verify(testRunner);
    verify(intRunner);
    verify(cleanRunner);
  }

  private Runnable makeRunnerMock(String targetName) {
    Runnable runner = createStrictMock(Runnable.class);
    runner.run();
    expectLastCall().andAnswer(() -> this.calledTargets.add(targetName));
    replay(runner);
    return runner;
  }

  private Runnable makeUncalledRunnerMock() {
    Runnable runner = createStrictMock(Runnable.class);
    replay(runner);
    return runner;
  }
}
