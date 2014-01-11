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

import org.savantbuild.dep.domain.License;
import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.output.Output;
import org.savantbuild.parser.ParseException;

import java.util.Map;
import java.util.Properties;

import groovy.lang.Closure;
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

  public Output output;

  public Project project;

  /**
   * Sets up the project information in the build file. This method is called with a Map of values and a closure like
   * this:
   * <p>
   * <pre>
   *   project(group: "org.example", name: "my-project", version: "1.1") {
   *
   *   }
   * </pre>
   * <p>
   * The require attributes are:
   * <p>
   * <pre>
   *   group: The name of the group that the project belongs to
   *   name: The name of the project
   *   version: The semantic version of the project.
   * </pre>
   *
   * @param attributes The attributes.
   * @return The project.
   */
  public Project project(Map<String, Object> attributes, Closure closure) {
    if (!GroovyTools.hasAttributes(attributes, "group", "name", "version", "license")) {
      throw new ParseException("Invalid project definition. It should look like:\n\n" +
          "  project(group: \"org.example\", name: \"my-project\", version: \"1.1\", license: \"Commercial\")");
    }

    project.group = GroovyTools.toString(attributes, "group");
    project.name = GroovyTools.toString(attributes, "name");

    String licenseStr = GroovyTools.toString(attributes, "license");
    try {
      project.license = License.valueOf(licenseStr);
    } catch (IllegalArgumentException e) {
      throw new ParseException("Invalid license [" + licenseStr + "]. It must be one of these values " + asList(License.values()));
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
   * Adds a target to the project. This method is called with a Map of values and a closure like this:
   * <p>
   * <pre>
   *   target(name: "compile", description: "Compiles") {
   *     ...
   *   }
   * </pre>
   * <p>
   * The required attributes are:
   * <p>
   * <pre>
   *   name: The name of the target
   * </pre>
   *
   * @param attributes The attributes of the target.
   * @param closure    The closure that contains the executable pieces of the target.
   * @return The Target.
   */
  public Target target(Map<String, Object> attributes, Closure closure) {
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