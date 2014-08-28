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
package org.savantbuild.runtime;

import java.util.HashSet;
import java.util.Set;

import org.savantbuild.dep.LicenseException;
import org.savantbuild.dep.PublishException;
import org.savantbuild.dep.domain.CompatibilityException;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.dep.workflow.ArtifactMetaDataMissingException;
import org.savantbuild.dep.workflow.ArtifactMissingException;
import org.savantbuild.dep.workflow.process.ProcessFailureException;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.output.Output;
import org.savantbuild.security.MD5Exception;
import org.savantbuild.util.CyclicException;

/**
 * Default project object runner. Using the {@link Project} object, this executes build targets of the project.
 *
 * @author Brian Pontarelli
 */
public class DefaultProjectRunner implements ProjectRunner {
  private final Output output;

  public DefaultProjectRunner(Output output) {
    this.output = output;
  }

  /**
   * Runs the targets by finding each target and then performing a graph traversal of that targets dependencies. This
   * ensures that a target is not called twice.
   *
   * @param project The project.
   * @param targets The targets to run.
   * @throws ArtifactMetaDataMissingException If any dependencies of the project are missing an AMD file in the
   *                                          repository or local cache.
   * @throws ArtifactMissingException         If any dependencies of the project are missing in the repository or local
   *                                          cache.
   * @throws BuildRunException                If the build can not be run (internally not due to a failure of the build
   *                                          itself).
   * @throws BuildFailureException            If the build fails while running.
   * @throws CompatibilityException           If the project has incompatible versions of a dependency.
   * @throws CyclicException                  If the project has cyclic dependencies.
   * @throws LicenseException                 If the project has a dependency with an invalid license.
   * @throws MD5Exception                     If a dependency is corrupt.
   * @throws PublishException                 If there was an error publishing an artifact.
   * @throws ProcessFailureException          If the downloading of a dependency fails.
   * @throws VersionException                 If any of the versions are not semantic.
   */
  @Override
  public void run(Project project, Iterable<String> targets) throws ArtifactMetaDataMissingException, ArtifactMissingException,
      BuildRunException, BuildFailureException, CompatibilityException, CyclicException, LicenseException, MD5Exception,
      ProcessFailureException, PublishException, VersionException {
    Set<String> calledTargets = new HashSet<>();
    targets.forEach((targetName) -> {
      Target target = project.targets.get(targetName);
      if (target == null) {
        output.error("Invalid target [" + targetName + "]");
        throw new BuildRunException("Invalid target [" + targetName + "]");
      }

      // Traverse the target dependency graph if the target has dependencies (is in the graph)
      if (project.targetGraph.contains(target)) {
        project.targetGraph.traverseUp(target, (origin, destination, edge, depth) -> {
          if (calledTargets.contains(destination.name)) {
            return;
          }

          runTarget(destination, calledTargets);
        });
      }

      runTarget(target, calledTargets);
    });
  }

  private void runTarget(Target target, Set<String> calledTargets) {
    output.info(":[%s]:", target.name);
    target.invocation.run();
    calledTargets.add(target.name);
    output.info("");
  }
}
