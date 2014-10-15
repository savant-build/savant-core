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

import static org.savantbuild.runtime.RuntimeConfiguration.DEBUG_SWITCH;

/**
 * Default implementation that uses a simple brute force approach for now.
 *
 * @author Brian Pontarelli
 */
public class DefaultRuntimeConfigurationParser implements RuntimeConfigurationParser {
  /**
   * Parses the command-line arguments. There are currently 4 fixed arguments:
   * <p>
   * <pre>
   *   --noColor = Disables the colorized output of Savant
   *   --debug = Enables debug output
   *   --version = Displays the version
   *   --help = Displays the help message
   *   --listTargets = Lists the build targets
   * </pre>
   * <p>
   * If any other argument starts with {@code --} then it is considered a switch. Switches can optionally have values
   * using the equals sign like this:
   * <p>
   * <pre>
   *   --switch=value
   * </pre>
   * <p>
   * All other arguments are considered targets to execute.
   * <p>
   * This parser does care about ordering of the arguments at all.
   *
   * @param arguments The CLI arguments.
   * @return The RuntimeConfiguration and never null.
   */
  @Override
  public RuntimeConfiguration parse(String... arguments) {
    RuntimeConfiguration configuration = new RuntimeConfiguration();
    for (String argument : arguments) {
      if (argument.equals("--noColor")) {
        configuration.colorizeOutput = false;
      } else if (argument.equals(DEBUG_SWITCH)) {
        configuration.debug = true;
      } else if (argument.equals("--help")) {
        configuration.help = true;
      } else if (argument.equals("--listTargets")) {
        configuration.listTargets = true;
      } else if (argument.equals("--version")) {
        configuration.printVersion = true;
      } else if (argument.startsWith("--")) {
        int equals = argument.indexOf('=');
        if (equals == -1) {
          configuration.switches.add(argument.substring(2));
        } else {
          configuration.switches.add(argument.substring(2, equals), argument.substring(equals + 1));
        }
      } else {
        configuration.targets.add(argument);
      }
    }

    return configuration;
  }
}
