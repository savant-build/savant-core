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

import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.parser.ParseException;

import java.util.Map;

import groovy.lang.Closure;

/**
 * Groovy delegate that defines the dependencies.
 *
 * @author Brian Pontarelli
 */
public class DependenciesDelegate {
  private final Dependencies dependencies;

  public DependenciesDelegate(Dependencies dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * Defines a group in the dependencies. This takes a Map of attributes but only the {@code type} attribute is
   * required. The {@code export} attribute is optional. This also takes a closure that defines the dependencies.
   *
   * @param attributes The attributes
   * @param closure    The closure that defines the dependencies.
   * @return The dependency group object.
   */
  public DependencyGroup group(Map<String, Object> attributes, Closure closure) {
    if (!GroovyTools.hasAttributes(attributes, "type")) {
      throw new ParseException("Invalid group definition. It must have a type attribute like this:\n\n" +
          "  group(type: \"compile\") {\n" +
          "    ...\n" +
          "  }");
    }

    String type = GroovyTools.toString(attributes, "type");
    boolean export = !attributes.containsKey("export") || Boolean.parseBoolean(attributes.get("export").toString());
    DependencyGroup group = new DependencyGroup(type, export);
    dependencies.groups.put(type, group);

    closure.setDelegate(new DependencyDelegate(group));
    closure.run();

    return group;
  }
}
