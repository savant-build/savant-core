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
package org.savantbuild.parser.groovy;

import org.savantbuild.BaseTest;
import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.URLProcess;
import org.savantbuild.domain.Project;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the groovy build file parser.
 *
 * @author Brian Pontarelli
 */
public class GroovyBuildFileParserTest extends BaseTest {
  @Test
  public void parse() {
    GroovyBuildFileParser parser = new GroovyBuildFileParser();
    Project project = parser.parse(projectDir.resolve("src/java/test/org/savantbuild/parser/groovy/simple.savant"));
    assertEquals(project.group, "group");
    assertEquals(project.name, "name");
    assertEquals(project.version, new Version("1.1"));

    assertEquals(project.targets.get("compile").name, "compile");
    assertEquals(project.targets.get("compile").description, "This target compiles everything");
    assertNotNull(project.targets.get("compile").invocation);

    project.targets.get("compile").invocation.run();
    assertEquals(project.name, "changed");

    assertEquals(project.workflow.fetchWorkflow.processes.size(), 2);
    assertTrue(project.workflow.fetchWorkflow.processes.get(0) instanceof CacheProcess);
    assertEquals(((CacheProcess) project.workflow.fetchWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");
    assertEquals(((URLProcess) project.workflow.fetchWorkflow.processes.get(1)).url, "http://repository.savantbuild.org");
    assertTrue(project.workflow.fetchWorkflow.processes.get(1) instanceof URLProcess);
    assertEquals(project.workflow.publishWorkflow.processes.size(), 1);
    assertEquals(((CacheProcess) project.workflow.publishWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");
  }
}
