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
package org.savantbuild.domain;

import org.savantbuild.BaseTest;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactMetaData;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.Publication;
import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the project domain.
 *
 * @author Brian Pontarelli
 */
public class ProjectTest extends BaseTest {
  @Test
  public void integrate() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/integration"));

    Project project = new Project(projectDir, output);
    project.publications.add(
        new Publication(new Artifact("group:name:name:1.1.1:jar", License.BSD),
            new ArtifactMetaData(null, License.BSD), projectDir.resolve("LICENSE"), projectDir.resolve("README.md"))
    );
    project.workflow = new Workflow(
        new FetchWorkflow(output),
        new PublishWorkflow(new CacheProcess(output, projectDir.resolve("build/test/integration").toString()))
    );

    project.integrate();
    Path integrationFile = projectDir.resolve("build/test/integration/group/name/1.1.1/name-1.1.1.jar");
    Path integrationSourceFile = projectDir.resolve("build/test/integration/group/name/1.1.1/name-1.1.1-src.jar");
    assertTrue(Files.isRegularFile(integrationFile));
    assertTrue(Files.isRegularFile(integrationSourceFile));
    assertEquals(Files.readAllBytes(integrationFile), Files.readAllBytes(projectDir.resolve("LICENSE")));
    assertEquals(Files.readAllBytes(integrationSourceFile), Files.readAllBytes(projectDir.resolve("README.md")));
  }

  @Test
  public void toArtifact() {
    Project project = new Project(projectDir, output);
    project.group = "group";
    project.name = "name";
    project.version = new Version("1.1.1");
    project.license = License.BSD;
    assertEquals(project.toArtifact(), new Artifact("group:name:name:1.1.1:jar", License.BSD));
  }
}
