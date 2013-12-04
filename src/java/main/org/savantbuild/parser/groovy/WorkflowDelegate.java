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
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.Process;
import org.savantbuild.dep.workflow.process.URLProcess;

import java.util.List;

import groovy.lang.Closure;

/**
 * Groovy delegate that captures the Workflow configuration from the project build file. The methods on this class
 * capture the configuration from the DSL.
 *
 * @author Brian Pontarelli
 */
public class WorkflowDelegate {
  public Workflow workflow;

  public WorkflowDelegate(Workflow workflow) {
    this.workflow = workflow;
  }

  /**
   * Configures the fetch workflow processes.
   *
   * @param closure The closure. This closure uses the delegate class {@link ProcessDelegate}.
   */
  public void fetch(Closure closure) {
    closure.setDelegate(new ProcessDelegate(workflow.fetchWorkflow.processes));
    closure.run();
  }

  /**
   * Configures the publish workflow processes.
   *
   * @param closure The closure. This closure uses the delegate class {@link ProcessDelegate}.
   */
  public void publish(Closure closure) {
    closure.setDelegate(new ProcessDelegate(workflow.publishWorkflow.processes));
    closure.run();
  }

  /**
   * Process delegate class that is used to configure {@link Process} instances for the {@link FetchWorkflow} and {@link
   * PublishWorkflow} of the {@link Workflow}.
   *
   * @author Brian Pontarelli
   */
  public static class ProcessDelegate {
    public List<Process> processes;

    public ProcessDelegate(List<Process> processes) {
      this.processes = processes;
    }

    /**
     * Adds a {@link URLProcess} to the workflow that uses the given URL. This URL must not require authentication.
     *
     * @param url The URL.
     */
    public void url(String url) {
      processes.add(new URLProcess(url, null, null));
    }

    /**
     * Adds a {@link URLProcess} to the workflow that uses the given protected URL with the given username and
     * password.
     *
     * @param url      The URL.
     * @param username The username to use with the URL.
     * @param password The password to use with the URL.
     */
    public void url(String url, String username, String password) {
      processes.add(new URLProcess(url, username, password));
    }

    /**
     * Adds a {@link CacheProcess} to the workflow that uses the default cache directory of
     * <code>${user.home}/.savant/cache</code>.
     */
    public void cache() {
      processes.add(new CacheProcess(null));
    }

    /**
     * Adds a {@link CacheProcess} to the workflow that uses the given cache directory.
     *
     * @param dir The cache directory.
     */
    public void cache(String dir) {
      processes.add(new CacheProcess(dir));
    }
  }
}
