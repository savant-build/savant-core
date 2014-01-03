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
import org.savantbuild.util.CyclicException;
import org.savantbuild.dep.io.MD5Exception;
import org.savantbuild.dep.workflow.ArtifactMetaDataMissingException;
import org.savantbuild.dep.workflow.ArtifactMissingException;
import org.savantbuild.dep.workflow.process.ProcessFailureException;
import org.savantbuild.domain.Project;
import org.savantbuild.parser.BuildFileParser;
import org.savantbuild.parser.ParseException;

import java.nio.file.Path;

/**
 * @author Brian Pontarelli
 */
public class DefaultBuildRunner implements BuildRunner {
  private final BuildFileParser buildFileParser;

  private final ProjectRunner projectRunner;

  public DefaultBuildRunner(BuildFileParser buildFileParser, ProjectRunner projectRunner) {
    this.buildFileParser = buildFileParser;
    this.projectRunner = projectRunner;
  }

  @Override
  public void run(Path buildFile, Iterable<String> targets)
  throws ArtifactMetaDataMissingException, ArtifactMissingException, BuildRunException, BuildFailureException,
  CompatibilityException, CyclicException, LicenseException, MD5Exception, ParseException, ProcessFailureException,
  VersionException {
    Project project = buildFileParser.parse(buildFile);
    projectRunner.run(project, targets);
  }
}
