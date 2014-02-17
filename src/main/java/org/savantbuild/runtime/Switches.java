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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that models command-line switches that might have values or not.
 *
 * @author Brian Pontarelli
 */
public class Switches {
  public Set<String> booleanSwitches = new HashSet<>();

  public Map<String, List<String>> valueSwitches = new HashMap<>();

  /**
   * Adds a switch that has no value (boolean).
   *
   * @param name The name of the switch.
   */
  public void add(String name) {
    booleanSwitches.add(name);
  }

  /**
   * Adds a switch with the given value.
   *
   * @param name  The name of the switch.
   * @param value The value of the switch.
   */
  public void add(String name, String value) {
    List<String> values = valueSwitches.get(name);
    if (values == null) {
      values = new ArrayList<>();
      valueSwitches.put(name, values);
    }

    values.add(value);
  }

  /**
   * Returns whether or not the given switch was given by the user. This checks booleans and value switches.
   *
   * @param name The name of the switch.
   * @return True if the switch is present (either --foo or --foo=bar).
   */
  public boolean has(String name) {
    return booleanSwitches.contains(name) || valueSwitches.containsKey(name);
  }

  /**
   * Returns whether or not the given switch has the given value.
   *
   * @param name  The name of the switch.
   * @param value The value to check for.
   * @return True if the switch has the value.
   */
  public boolean hasValue(String name, String value) {
    List<String> values = valueSwitches.get(name);
    return values != null && values.contains(value);
  }

  /**
   * Returns the values for the given switch name. If the switch doesn't exist, this returns null.
   *
   * @param name The name of the switch.
   * @return The values or null.
   */
  public String[] values(String name) {
    List<String> values = valueSwitches.get(name);
    if (values == null) {
      return null;
    }

    return values.toArray(new String[values.size()]);
  }
}
