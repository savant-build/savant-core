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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.savantbuild.dep.DependencyService.TraversalRules;
import org.savantbuild.dep.DependencyService.TraversalRules.GroupTraversalRule;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.ReifiedArtifact;
import org.savantbuild.dep.graph.ArtifactGraph;
import org.savantbuild.dep.graph.DependencyGraph;
import org.savantbuild.dep.graph.ResolvedArtifactGraph;
import org.savantbuild.domain.Project;
import org.savantbuild.lang.Classpath;
import org.savantbuild.output.Output;
import org.savantbuild.runtime.BuildFailureException;
import org.savantbuild.runtime.RuntimeConfiguration;

/**
 * Default plugin loader that uses the Savant dependency service and a URLClassLoader to load the plugin.
 *
 * @author Brian Pontarelli
 */
public class DefaultPluginLoader implements PluginLoader {
  public static final TraversalRules RESOLVE_CONFIGURATION = new TraversalRules()
      .with("compile", new GroupTraversalRule(true, "compile", "runtime"))
      .with("runtime", new GroupTraversalRule(true, "compile", "runtime"));

  private final Output output;

  private final Project project;

  private final RuntimeConfiguration runtimeConfiguration;

  public DefaultPluginLoader(Project project, RuntimeConfiguration runtimeConfiguration, Output output) {
    this.output = output;
    this.project = project;
    this.runtimeConfiguration = runtimeConfiguration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Plugin load(Artifact pluginDependency) {
    output.debugln("Loading plugin [%s]", pluginDependency);

    if (project.workflow == null || project.workflow.fetchWorkflow == null || project.workflow.fetchWorkflow.processes.isEmpty() ||
        project.workflow.publishWorkflow == null || project.workflow.publishWorkflow.processes.isEmpty()) {
      output.errorln("""
          Your project uses plugins but doesn't have a workflow defined to fetch them. Define a workflow in your project definition section like this:

            project(...) {
              workflow {
                standard()
              }
            }""");
      throw new BuildFailureException();
    }

    // This doesn't use the project as the root because the project might be in the graph and that would cause failures.
    // This is how Savant is self building
    ReifiedArtifact root = new ReifiedArtifact("__savantLoadPluginGroup__:__savantLoadPluginName__:0.0.0", License.Licenses.get("ApacheV2_0"));
    Dependencies dependencies = new Dependencies(new DependencyGroup("runtime", false, pluginDependency));
    DependencyGraph dependencyGraph = project.dependencyService.buildGraph(root, dependencies, project.workflow);
    ArtifactGraph artifactGraph = project.dependencyService.reduce(dependencyGraph);
    ResolvedArtifactGraph resolvedArtifactGraph = project.dependencyService.resolve(artifactGraph, project.workflow, RESOLVE_CONFIGURATION);

    Path pluginJarFilePath = resolvedArtifactGraph.getPath(pluginDependency.id);
    String pluginClassName = null;
    try (JarFile pluginJarFile = new JarFile(pluginJarFilePath.toFile())) {
      Manifest manifest = pluginJarFile.getManifest();
      if (manifest == null) {
        throw new PluginLoadException("Invalid plugin [" + pluginDependency + "]. The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class");
      }

      pluginClassName = manifest.getMainAttributes().getValue("Savant-Plugin-Class");
      if (pluginClassName == null) {
        throw new PluginLoadException("Invalid plugin [" + pluginDependency + "]. The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class");
      }

      Classpath classpath = resolvedArtifactGraph.toClasspath();
      output.debugln("Classpath for plugin [%s] is [%s]", pluginDependency, classpath);

      // URLClassLoader is closeable, but we need to keep it open while Savant is running. Therefore, we do not wrap this
      // in a try-with-resource block
      @SuppressWarnings("resource") URLClassLoader pluginClassLoader = classpath.toURLClassLoader();
      Class<?> pluginClass = pluginClassLoader.loadClass(pluginClassName);
      return (Plugin) pluginClass.getConstructor(Project.class, RuntimeConfiguration.class, Output.class).newInstance(project, runtimeConfiguration, output);
    } catch (IOException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin JAR could not be read", e);
    } catch (ClassNotFoundException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] was not in the plugin JAR", e);
    } catch (ClassCastException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] does not extend org.savantbuild.plugin.groovy.Plugin", e);
    } catch (NoSuchMethodException | InstantiationException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] could not be instantiated. " +
          "It must have a public constructor like this:\n\npublic MyPlugin(Project project, RuntimeConfiguration runtimeConfiguration, Output output) {\n  ...\n}\n", e);
    } catch (IllegalAccessException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] could not be instantiated", e);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }

      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] could not be instantiated", e);
    }
  }
}
