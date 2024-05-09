/*
 * Copyright (c) 2014-2024, Inversoft Inc., All Rights Reserved
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

import org.savantbuild.BaseUnitTest;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.dep.domain.CompatibilityException;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.ReifiedArtifact;
import org.savantbuild.dep.graph.DependencyEdgeValue;
import org.savantbuild.dep.graph.DependencyGraph;
import org.savantbuild.dep.graph.DependencyGraph.Dependency;
import org.savantbuild.domain.Version;
import org.savantbuild.output.SystemOutOutput;
import org.testng.annotations.Test;

/**
 * Tests the main entry point.
 *
 * @author Brian Pontarelli
 */
public class MainTest extends BaseUnitTest {
  @Test
  public void compatibilityErrorOutput() {
    ReifiedArtifact project = new ReifiedArtifact("org.savantbuild.test:project:1.0.0", License.parse("ApacheV2_0", null));
    ArtifactID leaf = new ArtifactID("org.savantbuild.test", "leaf", "leaf", "jar");
    ArtifactID intermediate = new ArtifactID("org.savantbuild.test", "intermediate", "intermediate", "jar");
    ArtifactID multipleVersions = new ArtifactID("org.savantbuild.test", "multiple-versions", "multiple-versions", "jar");
    ArtifactID multipleVersionsDifferentDeps = new ArtifactID("org.savantbuild.test", "multiple-versions-different-dependencies", "multiple-versions-different-dependencies", "jar");

    DependencyGraph incompatible = new DependencyGraph(project);
    incompatible.addEdge(new Dependency(project.id), new Dependency(multipleVersions), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.0.0"), "compile", new License()));
    incompatible.addEdge(new Dependency(project.id), new Dependency(intermediate), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.0.0"), "runtime", new License()));
    incompatible.addEdge(new Dependency(project.id), new Dependency(multipleVersionsDifferentDeps), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.0.0"), "compile", new License()));
    incompatible.addEdge(new Dependency(intermediate), new Dependency(multipleVersions), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.1.0"), "compile", new License()));
    incompatible.addEdge(new Dependency(intermediate), new Dependency(multipleVersionsDifferentDeps), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.1.0"), "runtime", new License()));
    incompatible.addEdge(new Dependency(multipleVersions), new Dependency(leaf), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.0.0"), "compile", new License()));
    incompatible.addEdge(new Dependency(multipleVersions), new Dependency(leaf), new DependencyEdgeValue(new Version("1.1.0"), new Version("1.0.0"), "compile", new License()));
    incompatible.addEdge(new Dependency(multipleVersionsDifferentDeps), new Dependency(leaf), new DependencyEdgeValue(new Version("1.0.0"), new Version("1.0.0"), "runtime", new License()));
    incompatible.addEdge(new Dependency(multipleVersionsDifferentDeps), new Dependency(leaf), new DependencyEdgeValue(new Version("1.1.0"), new Version("2.0.0"), "compile", new License()));

    output = new SystemOutOutput(true);
    Main.printCompatibilityError(new CompatibilityException(incompatible, new Dependency(leaf), new Version("1.0.0"), new Version("2.0.0")), output);
  }

  @Test(enabled = false)
  public void lineNumber() {
    Main.projectDir = projectDir.resolve("src/test/java/org/savantbuild/runtime");
    Main.main("compile");
  }
}
