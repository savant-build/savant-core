/*
 * Copyright (c) 2013-2024, Inversoft Inc., All Rights Reserved
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

import java.util.Map;

import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.domain.Version;
import org.savantbuild.parser.ParseException;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy delegate that defines the dependencies.
 *
 * @author Brian Pontarelli
 */
public class DependenciesDelegate {
  private final Dependencies dependencies;

  private final Map<String, Version> semanticVersionMappings;

  public DependenciesDelegate(Dependencies dependencies,
                              Map<String, Version> semanticVersionMappings) {
    this.dependencies = dependencies;
    this.semanticVersionMappings = semanticVersionMappings;
  }

  /**
   * Defines a group in the dependencies. This takes a Map of attributes but only the {@code type} attribute is
   * required. The {@code export} attribute is optional. This also takes a closure that defines the dependencies.
   *
   * @param attributes The attributes
   * @param closure    The closure that defines the dependencies.
   * @return The dependency group object.
   */
  public DependencyGroup group(Map<String, Object> attributes, @DelegatesTo(DependencyDelegate.class) Closure<?> closure) {
    if (!GroovyTools.hasAttributes(attributes, "name")) {
      throw new ParseException("Invalid group definition. It must have a [name] attribute like this:\n\n" +
          "  group(name: \"compile\") {\n" +
          "    ...\n" +
          "  }");
    }

    String name = GroovyTools.toString(attributes, "name");
    boolean export = !attributes.containsKey("export") || Boolean.parseBoolean(attributes.get("export").toString());
    DependencyGroup group = new DependencyGroup(name, export);
    dependencies.groups.put(name, group);

    closure.setDelegate(new DependencyDelegate(group, semanticVersionMappings));
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.run();

    return group;
  }
}
