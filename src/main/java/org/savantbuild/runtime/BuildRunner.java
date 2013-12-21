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

import org.savantbuild.dep.LicenseException;
import org.savantbuild.dep.domain.CompatibilityException;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.dep.graph.CyclicException;
import org.savantbuild.dep.io.MD5Exception;
import org.savantbuild.dep.workflow.ArtifactMetaDataMissingException;
import org.savantbuild.dep.workflow.ArtifactMissingException;
import org.savantbuild.dep.workflow.process.ProcessFailureException;
import org.savantbuild.parser.ParseException;

import java.nio.file.Path;

/**
 * Runs the build starting from a build file.
 *
 * @author Brian Pontarelli
 */
public interface BuildRunner {
  /**
   * Loads the given build file and executes the given targets in the build file.
   *
   * @param buildFile The build file.
   * @param targets   The targets to execute.
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
   * @throws ParseException                   If the build file can not be parsed.
   * @throws ProcessFailureException          If the downloading of a dependency fails.
   * @throws VersionException                 If any of the versions are not semantic.
   */
  void run(Path buildFile, Iterable<String> targets) throws ArtifactMetaDataMissingException, ArtifactMissingException,
      BuildRunException, BuildFailureException, CompatibilityException, CyclicException, LicenseException, MD5Exception,
      ParseException, ProcessFailureException, VersionException;
}
