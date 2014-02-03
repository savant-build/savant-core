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
package org.savantbuild.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.savantbuild.output.Output;
import org.savantbuild.output.SystemOutOutput;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.groovy.GroovyBuildFileParser;

/**
 * Main entry point for Savant CLI runtime.
 *
 * @author Brian Pontarelli
 */
public class Main {
  /**
   * THe main method.
   *
   * @param args CLI arguments.
   */
  public static void main(String... args) {
    RuntimeConfigurationParser runtimeConfigurationParser = new DefaultRuntimeConfigurationParser();
    RuntimeConfiguration runtimeConfiguration = runtimeConfigurationParser.parse(args);
    Output output = new SystemOutOutput(runtimeConfiguration.colorizeOutput);
    if (runtimeConfiguration.debug) {
      output.enableDebug();
    }

    Path buildFile = Paths.get("build.savant");
    if (!Files.isRegularFile(buildFile) || !Files.isReadable(buildFile)) {
      output.error("Build file [build.savant] is missing or not readable.");
      System.exit(1);
    }

    try {
      BuildRunner buildRunner = new DefaultBuildRunner(output, new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder()), new DefaultProjectRunner(output));
      buildRunner.run(buildFile, runtimeConfiguration);
    } catch (Exception e) {
      output.error("Build failed due to exception [%s]. Enable debug to see the stack trace.", e.getMessage());
      System.exit(1);
    }
  }
}
