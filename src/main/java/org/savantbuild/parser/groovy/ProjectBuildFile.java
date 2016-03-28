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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.output.Output;
import org.savantbuild.parser.ParseException;
import org.savantbuild.plugin.DefaultPluginLoader;
import org.savantbuild.plugin.Plugin;
import org.savantbuild.plugin.PluginLoader;
import org.savantbuild.runtime.BuildFailureException;
import org.savantbuild.runtime.RuntimeConfiguration;
import org.savantbuild.runtime.Switches;
import org.savantbuild.util.MapBuilder;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import static java.util.Arrays.asList;

/**
 * Base class from the project build file Groovy script.
 *
 * @author Brian Pontarelli
 */
public abstract class ProjectBuildFile extends Script {
  public final Map<String, String> ENV = System.getenv();

  public final Properties SYS = System.getProperties();

  public final GlobalConfiguration global = new GlobalConfiguration();

  public Output output;

  public Project project;

  public RuntimeConfiguration runtimeConfiguration;

  public Switches switches;

  @Override
  public Object getProperty(String property) {
    try {
      return super.getProperty(property);
    } catch (MissingPropertyException e) {
      throw new MissingPropertyException("You build file is attempting to access the property [" + property + "]. You might have forgotten to import a plugin or define this property or it could be a typo.", property, String.class);
    }
  }

  /**
   * Fails the build with the given message by throwing a {@link BuildFailureException}.
   *
   * @param message The failure message.
   * @param values  Values used to format the message.
   */
  protected void fail(String message, Object... values) {
    output.errorln(message, values);
    throw new BuildFailureException();
  }

  /**
   * <p> Loads a plugin and returns a new instance of the Plugin class. This method is called with the information used
   * to load the plugin like this: </p>
   * <pre>
   *   java = loadPlugin(id: "org.savantbuild.plugin:java:0.1.0")
   * </pre>
   *
   * @param attributes The Attributes used to load the plugin.
   * @return The Plugin instance.
   */
  protected Plugin loadPlugin(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "id")) {
      throw new ParseException("Invalid loadPlugin call. You must supply the id of the plugin to load like this:\n\n" +
          "  groovy = loadPlugin(id: \"org.savantbuild.plugin:groovy:0.1.0\")");
    }

    String id = GroovyTools.toString(attributes, "id");
    PluginLoader loader = new DefaultPluginLoader(project, runtimeConfiguration, output);
    Artifact pluginDependency = new Artifact(id, false);
    Plugin plugin = loader.load(pluginDependency);
    project.plugins.put(pluginDependency, plugin);
    return plugin;
  }

  /**
   * <p> Sets up the project information in the build file. This method is called with a Map of values and a closure
   * like this: </p>
   * <pre>
   *   project(group: "org.example", name: "my-project", version: "1.1", licenses: ["Commercial"]) {
   *
   *   }
   * </pre>
   * <p> The require attributes are: </p>
   * <pre>
   *   group: The name of the group that the project belongs to
   *   name: The name of the project
   *   version: The semantic version of the project.
   *   licenses: The license(s) of the project.
   * </pre>
   *
   * @param attributes The attributes.
   * @param closure    The closure that is invoked for the project configuration handling.
   * @return The project.
   */
  protected Project project(Map<String, Object> attributes, @DelegatesTo(ProjectDelegate.class) Closure closure) {
    List<String> attrs = asList("group", "name", "version", "licenses");
    Map<String, Class<?>> attrTypes = new MapBuilder<String, Class<?>>().put("group", String.class)
                                                                        .put("name", String.class)
                                                                        .put("version", String.class)
                                                                        .put("licenses", List.class)
                                                                        .done();
    if (!GroovyTools.attributesValid(attributes, attrs, attrs, attrTypes)) {
      throw new ParseException("Invalid project definition. One of the required attributes is missing (i.e. licenses). It should look like:\n\n" +
          "  project(group: \"org.example\", name: \"my-project\", version: \"1.1\", licenses: [\"Commercial\"])");
    }

    project.group = GroovyTools.toString(attributes, "group");
    project.name = GroovyTools.toString(attributes, "name");

    List<String> licenseNames = GroovyTools.toListOfStrings(attributes.get("licenses"));
    if (licenseNames == null || licenseNames.isEmpty()) {
      throw new ParseException("Invalid project definition. The [licenses] attribute is missing. It should look like:\n\n" +
          "  project(group: \"org.example\", name: \"my-project\", version: \"1.1\", licenses: [\"Commercial\"])");
    }

    for (String licenseName : licenseNames) {
      try {
        License license = License.valueOf(licenseName);
        String text = null;
        Path licenseOverrideFile = project.directory.resolve("license-" + licenseName + ".txt");
        if (Files.isRegularFile(licenseOverrideFile)) {
          text = new String(Files.readAllBytes(licenseOverrideFile));
        }

        if (license.requiresText && text == null) {
          throw new ParseException("Invalid license configuration. You specified the [" + license + "] but did not provide a [license-" + license +
              ".txt] file in the root of your project. This license requires a custom license definition.");
        }

        project.licenses.put(license, text);
      } catch (IllegalArgumentException e) {
        throw new ParseException("Invalid license [" + licenseName + "]. It must be one of these values " + asList(License.values()));
      } catch (IOException e) {
        throw new ParseException("Unable to load the license override file [license-" + licenseName + ".txt].", e);
      }
    }

    String versionStr = GroovyTools.toString(attributes, "version");
    try {
      project.version = new Version(versionStr);
    } catch (VersionException e) {
      throw new ParseException("Invalid project version [" + versionStr + "]. You must specify a valid Savant version (semantic version).");
    }

    closure.setDelegate(new ProjectDelegate(output, project));
    closure.run();

    return project;
  }

  /**
   * <p> Adds a target to the project. This method is called with a Map of values and a closure like this: </p>
   * <pre>
   *   target(name: "compile", description: "Compiles") {
   *     ...
   *   }
   * </pre>
   * <p> The required attributes are: </p>
   * <pre>
   *   name: The name of the target
   * </pre>
   *
   * @param attributes The attributes of the target.
   * @param closure    The closure that contains the executable pieces of the target.
   * @return The Target.
   */
  protected Target target(Map<String, Object> attributes, Closure closure) {
    if (!GroovyTools.hasAttributes(attributes, "name")) {
      throw new ParseException("Invalid target definition. It should look like:\n\n" +
          "  target(name: \"compile\") {\n" +
          "  }");
    }

    Target target = new Target();
    target.name = GroovyTools.toString(attributes, "name");
    target.description = GroovyTools.toString(attributes, "description");
    target.invocation = closure;
    target.dependencies = GroovyTools.toListOfStrings(attributes.get("dependsOn"));

    project.targets.put(target.name, target);
    return target;
  }
}
