/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.dep.domain.ArtifactMetaData;
import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.Publication;
import org.savantbuild.dep.domain.ReifiedArtifact;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.SVNProcess;
import org.savantbuild.dep.workflow.process.URLProcess;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publications;
import org.savantbuild.domain.Target;
import org.savantbuild.domain.Version;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.runtime.RuntimeConfiguration;
import org.savantbuild.util.Graph;
import org.savantbuild.util.HashGraph;
import org.testng.annotations.Test;

import groovy.lang.MissingPropertyException;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the groovy build file parser.
 *
 * @author Brian Pontarelli
 */
public class GroovyBuildFileParserTest extends BaseUnitTest {
  @Test
  public void parse() {
    GroovyBuildFileParser parser = new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder());
    Path buildFile = projectDir.resolve("src/test/java/org/savantbuild/parser/groovy/simple.savant");
    Project project = parser.parse(buildFile, new RuntimeConfiguration());
    assertEquals(project.group, "group");
    assertEquals(project.name, "name");
    assertEquals(project.version, new Version("1.1"));
    assertEquals(project.licenses.get(0).identifier, "Apache-2.0");
    assertEquals(project.licenses.get(1).identifier, "Apache-1.0");
    assertEquals(project.licenses.get(2).identifier, "BSD-2-Clause");
    assertEquals(project.licenses.get(2).text, "BSD license");
    assertEquals(project.licenses.get(3).identifier, "Commercial");
    assertEquals(project.licenses.get(3).text, "Commercial license file.");

    // Verify the targets
    assertEquals(project.targets.get("compile").name, "compile");
    assertEquals(project.targets.get("compile").description, "This target compiles everything");
    assertNotNull(project.targets.get("compile").invocation);
    assertNull(project.targets.get("compile").dependencies);

    assertEquals(project.targets.get("test").name, "test");
    assertEquals(project.targets.get("test").description, "This runs the tests");
    assertNotNull(project.targets.get("test").invocation);
    assertEquals(project.targets.get("test").dependencies, asList("compile"));

    // Verify the target graph
    Graph<Target, Object> expected = new HashGraph<>();
    expected.addEdge(project.targets.get("test"), project.targets.get("compile"), Project.GRAPH_EDGE);
    assertEquals(project.targetGraph, expected);

    // Verify the target executes correctly
    project.targets.get("compile").invocation.run();
    assertEquals(project.name, "changed");

    // Verify the workflow
    assertEquals(project.workflow.fetchWorkflow.processes.size(), 2);
    assertTrue(project.workflow.fetchWorkflow.processes.get(0) instanceof CacheProcess);
    assertEquals(((CacheProcess) project.workflow.fetchWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");
    assertEquals(((URLProcess) project.workflow.fetchWorkflow.processes.get(1)).url, "http://repository.savantbuild.org");
    assertEquals(((URLProcess) project.workflow.fetchWorkflow.processes.get(1)).username, "username");
    assertEquals(((URLProcess) project.workflow.fetchWorkflow.processes.get(1)).password, "password");
    assertEquals(project.workflow.publishWorkflow.processes.size(), 1);
    assertEquals(((CacheProcess) project.workflow.publishWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");

    // Verify the PublishWorkflow
    assertEquals(project.publishWorkflow.processes.size(), 1);
    assertTrue(project.publishWorkflow.processes.get(0) instanceof SVNProcess);
    assertEquals(((SVNProcess) project.publishWorkflow.processes.get(0)).repository, "http://svn.example.com");
    assertEquals(((SVNProcess) project.publishWorkflow.processes.get(0)).username, "svn-username");
    assertEquals(((SVNProcess) project.publishWorkflow.processes.get(0)).password, "svn-password");

    // Verify the dependencies
    Dependencies expectedDependencies = new Dependencies(
        new DependencyGroup("compile", true, new Artifact("org.example:compile:1.0", false)),
        new DependencyGroup("test-compile", false, new Artifact("org.example:test:1.0", false), new Artifact("org.example:test2:2.0", false)));
    assertEquals(project.dependencies, expectedDependencies);

    // Verify the publications
    List<License> licenses = Arrays.asList(
        License.parse("Apache-2.0", null),
        License.parse("Apache-1.0", null),
        License.parse("BSD-2-Clause", "BSD license"),
        License.parse("Commercial", "Commercial license file.")
    );
    Publications expectedPublications = new Publications();
    expectedPublications.add("main",
        new Publication(new ReifiedArtifact(new ArtifactID("group", "name", "publication1", "jar"), new Version("1.1"), licenses),
            new ArtifactMetaData(expectedDependencies, licenses),
            buildFile.getParent().resolve("build/jars/name-1.1.0.jar").toAbsolutePath(),
            buildFile.getParent().resolve("build/jars/name-1.1.0-src.jar").toAbsolutePath())
    );
    expectedPublications.add("main",
        new Publication(new ReifiedArtifact(new ArtifactID("group", "name", "publication3", "jar"), new Version("1.1"), licenses),
            new ArtifactMetaData(new Dependencies(), licenses),
            buildFile.getParent().resolve("build/jars/name-1.1.0.jar").toAbsolutePath(),
            buildFile.getParent().resolve("build/jars/name-1.1.0-src.jar").toAbsolutePath())
    );
    expectedPublications.add("test",
        new Publication(new ReifiedArtifact(new ArtifactID("group", "name", "publication2", "jar"), new Version("1.1"), licenses),
            new ArtifactMetaData(expectedDependencies, licenses),
            buildFile.getParent().resolve("build/jars/name-test-1.1.0.jar").toAbsolutePath(),
            buildFile.getParent().resolve("build/jars/name-test-1.1.0-src.jar").toAbsolutePath())
    );
    assertEquals(project.publications, expectedPublications);
  }

  @Test
  public void parseMissingPlugin() {
    GroovyBuildFileParser parser = new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder());
    Path buildFile = projectDir.resolve("src/test/java/org/savantbuild/parser/groovy/missing-plugin.savant");
    Project project = parser.parse(buildFile, new RuntimeConfiguration());

    try {
      project.targets.get("compile").invocation.run();
    } catch (MissingPropertyException e) {
      // Expected
      assertTrue(e.getMessage().contains("property [missingDependency]"));
    }
  }

  @Test
  public void parseWithSwitches() {
    GroovyBuildFileParser parser = new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder());
    Path buildFile = projectDir.resolve("src/test/java/org/savantbuild/parser/groovy/simple.savant");
    RuntimeConfiguration runtimeConfiguration = new RuntimeConfiguration();
    runtimeConfiguration.switches.add("skip");
    Project project = parser.parse(buildFile, runtimeConfiguration);

    // Verify the target executes correctly
    project.targets.get("compile").invocation.run();
    assertEquals(project.name, "name");
  }
}
