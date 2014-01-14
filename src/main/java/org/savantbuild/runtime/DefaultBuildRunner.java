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

import java.nio.file.Path;

import org.savantbuild.dep.LicenseException;
import org.savantbuild.dep.PublishException;
import org.savantbuild.dep.domain.CompatibilityException;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.dep.workflow.ArtifactMetaDataMissingException;
import org.savantbuild.dep.workflow.ArtifactMissingException;
import org.savantbuild.dep.workflow.process.ProcessFailureException;
import org.savantbuild.domain.Project;
import org.savantbuild.parser.BuildFileParser;
import org.savantbuild.parser.ParseException;
import org.savantbuild.plugin.PluginLoadException;
import org.savantbuild.security.MD5Exception;
import org.savantbuild.util.CyclicException;

import com.google.inject.Inject;

/**
 * Default build runner. This is essentially the main entry point for the build system. It takes a build file and a list
 * of targets and runs the build.
 * <p>
 * This implementation uses the main {@link BuildFileParser} to parse the build file into domain objects.
 * <p>
 * Once the build file is parsed, this uses the default {@link ProjectRunner} to run build on the project.
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildRunner implements BuildRunner {
  private final BuildFileParser buildFileParser;

  private final ProjectRunner projectRunner;

  @Inject
  DefaultBuildRunner(BuildFileParser buildFileParser, ProjectRunner projectRunner) {
    this.buildFileParser = buildFileParser;
    this.projectRunner = projectRunner;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(Path buildFile, RuntimeConfiguration runtimeConfiguration)
      throws ArtifactMetaDataMissingException, ArtifactMissingException,
      BuildRunException, BuildFailureException, CompatibilityException, CyclicException, LicenseException, MD5Exception,
      ParseException, PluginLoadException, ProcessFailureException, PublishException, VersionException {
    Project project = buildFileParser.parse(buildFile);
    projectRunner.run(project, runtimeConfiguration.targets);
  }
}
