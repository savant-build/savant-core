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
package org.savantbuild.plugin;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Version;
import org.savantbuild.output.Output;
import org.savantbuild.output.SystemOutOutput;
import org.savantbuild.runtime.RuntimeConfiguration;
import org.savantbuild.security.MD5;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the default plugin loader.
 *
 * @author Brian Pontarelli
 */
public class DefaultPluginLoaderTest extends BaseUnitTest {
  @BeforeClass
  public void generateMD5Files() throws Exception {
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-class/0.1.0/bad-class-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-class/0.1.0/bad-class-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-class/0.1.0/bad-class-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-class/0.1.0/bad-class-0.1.0.jar.amd.md5"));

    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-constructor/0.1.0/bad-constructor-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-constructor/0.1.0/bad-constructor-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-constructor/0.1.0/bad-constructor-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-constructor/0.1.0/bad-constructor-0.1.0.jar.amd.md5"));

    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-manifest/0.1.0/bad-manifest-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-manifest/0.1.0/bad-manifest-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-manifest/0.1.0/bad-manifest-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/bad-manifest/0.1.0/bad-manifest-0.1.0.jar.amd.md5"));

    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/good/0.1.0/good-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/good/0.1.0/good-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/good/0.1.0/good-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/good/0.1.0/good-0.1.0.jar.amd.md5"));

    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-class/0.1.0/missing-class-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-class/0.1.0/missing-class-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-class/0.1.0/missing-class-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-class/0.1.0/missing-class-0.1.0.jar.amd.md5"));

    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-manifest/0.1.0/missing-manifest-0.1.0.jar")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-manifest/0.1.0/missing-manifest-0.1.0.jar.md5"));
    MD5.writeMD5(MD5.forPath(projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-manifest/0.1.0/missing-manifest-0.1.0.jar.amd")),
        projectDir.resolve("src/test/plugin-repository/org/savantbuild/test/missing-manifest/0.1.0/missing-manifest-0.1.0.jar.amd.md5"));
  }

  @Test
  public void loadBadClass() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    try {
      loader.load(new Artifact("org.savantbuild.test:bad-class:0.1.0"));
      fail("Should have thrown an exception");
    } catch (PluginLoadException e) {
      assertTrue(e.getMessage().contains("does not extend"), e.getMessage());
      assertTrue(e.getCause() instanceof ClassCastException);
    }
  }

  @Test
  public void loadBadConstructor() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    try {
      loader.load(new Artifact("org.savantbuild.test:bad-constructor:0.1.0"));
      fail("Should have thrown an exception");
    } catch (PluginLoadException e) {
      assertTrue(e.getMessage().contains("could not be instantiated"));
      assertTrue(e.getCause() instanceof NoSuchMethodException);
    }
  }

  @Test
  public void loadBadManifest() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    try {
      loader.load(new Artifact("org.savantbuild.test:bad-manifest:0.1.0"));
      fail("Should have thrown an exception");
    } catch (PluginLoadException e) {
      assertTrue(e.getMessage().contains("The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class"));
      assertNull(e.getCause());
    }
  }

  @Test
  public void loadGood() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    GoodPlugin plugin = (GoodPlugin) loader.load(new Artifact("org.savantbuild.test:good:0.1.0"));
    assertSame(plugin.project, project);
    assertSame(plugin.output, output);
  }

  @Test
  public void loadMissingClass() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    try {
      loader.load(new Artifact("org.savantbuild.test:missing-class:0.1.0"));
      fail("Should have thrown an exception");
    } catch (PluginLoadException e) {
      assertTrue(e.getMessage().contains("was not in the plugin JAR"));
      assertTrue(e.getCause() instanceof ClassNotFoundException);
    }
  }

  @Test
  public void loadMissingManifest() {
    Output output = new SystemOutOutput(false);
    Project project = makeProject(output);
    DefaultPluginLoader loader = new DefaultPluginLoader(project, new RuntimeConfiguration(), output);

    try {
      loader.load(new Artifact("org.savantbuild.test:missing-manifest:0.1.0"));
      fail("Should have thrown an exception");
    } catch (PluginLoadException e) {
      assertTrue(e.getMessage().contains("The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class"));
      assertNull(e.getCause());
    }
  }

  private Project makeProject(Output output) {
    Project project = new Project(projectDir, output);
    project.group = "org.savantbuild.test";
    project.name = "plugin-loader-test";
    project.version = new Version("0.1.0");
    project.licenses.add(License.parse("BSD_2_Clause", null));
    String pluginDir = projectDir.resolve("src/test/plugin-repository").toString();
    project.workflow = new Workflow(
        new FetchWorkflow(output, new CacheProcess(output, pluginDir, integration.toString())),
        new PublishWorkflow(new CacheProcess(output, pluginDir, integration.toString())),
        output
    );
    return project;
  }
}
