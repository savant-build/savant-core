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

import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.domain.Project;
import org.savantbuild.parser.ParseException;

import java.util.Map;

import groovy.lang.Closure;

/**
 * Groovy delegate that captures the Project configuration from the project build file. The methods on this class
 * capture the configuration from the DSL.
 *
 * @author Brian Pontarelli
 */
public class ProjectDelegate {
  public Project project;

  public ProjectDelegate(Project project) {
    this.project = project;
  }

  /**
   * Configures a plugin that the project needs. This method is called with a Map of attributes that contains the
   * specification of the plugin's artifact and the local name of the plugin. It should look like:
   * <p>
   * <pre>
   *   plugin(localName: "plugin", artifact: "org.example:my-plugin:1.0")
   * </pre>
   *
   * @param attributes The attributes.
   */
  public void plugin(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "localName", "artifact")) {
      throw new ParseException("Invalid plugin definition. It should look like:\n\n" +
          "  plugin(localName: \"plugin\", artifact: \"org.example:my-plugin:1.0\")");
    }

    project.plugins.put(GroovyTools.toString(attributes, "localName"), GroovyTools.toString(attributes, "artifact"));
  }

  /**
   * Configures the project workflow. This method is called with a closure that contains the workflow definition. It
   * should look like:
   * <p>
   * <pre>
   *   workflow {
   *     fetch {
   *       cache()
   *       url(url: "http://repository.savantbuild.org")
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
}
