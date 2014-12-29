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

import java.util.List;
import java.util.Map;

import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.Process;
import org.savantbuild.dep.workflow.process.SVNProcess;
import org.savantbuild.dep.workflow.process.URLProcess;
import org.savantbuild.output.Output;
import org.savantbuild.parser.ParseException;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy delegate that captures the Workflow configuration from the project build file. The methods on this class
 * capture the configuration from the DSL.
 *
 * @author Brian Pontarelli
 */
public class WorkflowDelegate {
  public final Output output;

  public final Workflow workflow;

  public WorkflowDelegate(Output output, Workflow workflow) {
    this.output = output;
    this.workflow = workflow;
  }

  /**
   * Configures the fetch workflow processes.
   *
   * @param closure The closure. This closure uses the delegate class {@link ProcessDelegate}.
   */
  public void fetch(@DelegatesTo(ProcessDelegate.class) Closure closure) {
    closure.setDelegate(new ProcessDelegate(output, workflow.fetchWorkflow.processes));
    closure.run();
  }

  /**
   * Configures the publish workflow processes.
   *
   * @param closure The closure. This closure uses the delegate class {@link ProcessDelegate}.
   */
  public void publish(@DelegatesTo(ProcessDelegate.class) Closure closure) {
    closure.setDelegate(new ProcessDelegate(output, workflow.publishWorkflow.processes));
    closure.run();
  }

  /**
   * Configures the standard project workflow as follows:
   * <p>
   * <pre>
   *   fetch {
   *     cache()
   *     url(url: "http://savant.inversoft.org/repository")
   *   }
   *   publish {
   *     cache()
   *   }
   * </pre>
   */
  public void standard() {
    workflow.fetchWorkflow.processes.add(new CacheProcess(output, null));
    workflow.fetchWorkflow.processes.add(new URLProcess(output, "http://savant.inversoft.org", null, null));
    workflow.publishWorkflow.processes.add(new CacheProcess(output, null));
  }

  /**
   * Process delegate class that is used to configure {@link Process} instances for the {@link FetchWorkflow} and {@link
   * PublishWorkflow} of the {@link Workflow}.
   *
   * @author Brian Pontarelli
   */
  public static class ProcessDelegate {
    public final Output output;

    public final List<Process> processes;

    public ProcessDelegate(Output output, List<Process> processes) {
      this.output = output;
      this.processes = processes;
    }

    /**
     * Adds a {@link CacheProcess} to the workflow that uses the given attributes.
     *
     * @param attributes The attributes.
     */
    public void cache(Map<String, Object> attributes) {
      processes.add(new CacheProcess(output, GroovyTools.toString(attributes, "dir")));
    }

    /**
     * Adds a {@link SVNProcess} to the workflow that uses the given attributes.
     *
     * @param attributes The SVN attributes.
     */
    public void subversion(Map<String, Object> attributes) {
      if (!GroovyTools.hasAttributes(attributes, "repository")) {
        throw new ParseException("Invalid subversion workflow definition. It should look like:\n\n" +
            "  subversion(repository: \"http://svn.example.com\")");
      }

      processes.add(new SVNProcess(output, GroovyTools.toString(attributes, "repository"), GroovyTools.toString(attributes, "username"),
          GroovyTools.toString(attributes, "password")));
    }

    /**
     * Adds a {@link URLProcess} to the workflow that uses the given attributes.
     *
     * @param attributes The URL attributes.
     */
    public void url(Map<String, Object> attributes) {
      if (!GroovyTools.hasAttributes(attributes, "url")) {
        throw new ParseException("Invalid url workflow definition. It should look like:\n\n" +
            "  url(url: \"http://repository.savantbuild.org\")");
      }

      processes.add(new URLProcess(output, GroovyTools.toString(attributes, "url"), GroovyTools.toString(attributes, "username"),
          GroovyTools.toString(attributes, "password")));
    }
  }
}
