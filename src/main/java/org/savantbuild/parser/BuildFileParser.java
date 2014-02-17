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
package org.savantbuild.parser;

import java.nio.file.Path;

import org.savantbuild.domain.Project;
import org.savantbuild.runtime.RuntimeConfiguration;

/**
 * Parses the build file into the domain objects.
 *
 * @author Brian Pontarelli
 */
public interface BuildFileParser {
  /**
   * Parses the given file and generates the Project object.
   *
   * @param file The file.
   * @param runtimeConfiguration The runtime configuration that is passed to the build script.
   * @return The Project.
   */
  Project parse(Path file, RuntimeConfiguration runtimeConfiguration);
}
