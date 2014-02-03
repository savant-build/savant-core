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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime configuration for a Savant build run.
 *
 * @author Brian Pontarelli
 */
public class RuntimeConfiguration {
  /**
   * The list of command-line switches.
   */
  public List<Switch> arguments = new ArrayList<>();

  /**
   * Determines if the output should be colorized.
   */
  public boolean colorizeOutput = true;

  /**
   * Determines if debug output is enabled.
   */
  public boolean debug;

  /**
   * Determines if the user needs help.
   */
  public boolean help;

  /**
   * Determines if the targets in the project build file should be printed to the output.
   */
  public boolean listTargets;

  /**
   * The list of targets to execute (in order).
   */
  public List<String> targets = new ArrayList<>();

  public RuntimeConfiguration() {
  }

  public RuntimeConfiguration(boolean colorizeOutput, String... targets) {
    this.colorizeOutput = colorizeOutput;
    Collections.addAll(this.targets, targets);
  }

  /**
   * A class that models command-line switches that might have values or not.
   */
  public static class Switch {
    public String name;

    public String value;

    public Switch(String name) {
      this.name = name;
    }

    public Switch(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }
}
