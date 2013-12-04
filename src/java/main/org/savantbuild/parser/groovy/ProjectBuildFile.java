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

import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.parser.ParseException;

import java.util.Map;
import java.util.Properties;

import groovy.lang.Closure;
import groovy.lang.Script;

/**
 * Base class from the project build file Groovy script.
 *
 * @author Brian Pontarelli
 */
public abstract class ProjectBuildFile extends Script {
  public final Map<String, String> ENV = System.getenv();

  public final Properties SYS = System.getProperties();

  public Project project;

  /**
   * Sets up the project information in the build file. This method is called with a Map of values like this:
   * <p>
   * <pre>
   *   project(group: "org.example", name: "my-project", version: "1.1")
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
  public Project project(Map<String, String> attributes) {
    if (!attributes.containsKey("group") && !attributes.containsKey("name") && !attributes.containsKey("version")) {
      throw new ParseException("Invalid project definition. It should look like:\n\n" +
          "  project(group: \"org.example\", name: \"my-project\", version: \"1.1\")");
    }

    project.group = attributes.get("group");
    project.name = attributes.get("name");
    try {
      project.version = new Version(attributes.get("version"));
    } catch (VersionException e) {
      throw new ParseException("Invalid project version. You must specify a valid Savant version (semantic version).");
    }

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
    if (!attributes.containsKey("name")) {
      throw new ParseException("Invalid target definition. It should look like:\n\n" +
          "  target(name: \"compile\") {\n" +
          "  }");
    }

    Target target = new Target();
    target.name = safeToString(attributes.get("name"));
    target.description = safeToString(attributes.get("description"));
    target.invocation = closure;

    project.targets.put(target.name, target);
    return target;
  }

  /**
   * Configures the project workflow. This method is called with a closure that contains the workflow definition. It
   * should look like:
   * <p>
   * <pre>
   *   workflow {
   *     fetch {
   *       cache()
   *       url("http://repository.savantbuild.org")
   *     }
   *     publish {
   *       cache()
   *     }
   *   }
   * </pre>
   *
   * @param closure The closure that is called to setup the workflow configuration. This closure uses the delegate class
   *                {@link WorkflowDelegate}.
   * @return The workflow.
   */
  public Workflow workflow(Closure closure) {
    project.workflow = new Workflow(new FetchWorkflow(), new PublishWorkflow());
    closure.setDelegate(new WorkflowDelegate(project.workflow));
    closure.run();
    return project.workflow;
  }

  private String safeToString(Object value) {
    if (value == null) {
      return null;
    }

    return value.toString();
  }
}
