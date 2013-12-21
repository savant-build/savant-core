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

import org.savantbuild.dep.domain.Dependency;
import org.savantbuild.dep.domain.DependencyGroup;

/**
 * Groovy delegate that defines the dependencies.
 *
 * @author Brian Pontarelli
 */
public class DependencyDelegate {
  private final DependencyGroup group;

  public DependencyDelegate(DependencyGroup group) {
    this.group = group;
  }

  /**
   * Defines a dependency. This takes the specification for the dependency. This dependency is not optional.
   *
   * @param spec The specification for the dependency.
   * @return The dependency object.
   * @see Dependency#Dependency(String, boolean)
   */
  public Dependency dependency(String spec) {
    Dependency dependency = new Dependency(spec, false);
    group.dependencies.add(dependency);
    return dependency;
  }

  /**
   * Defines a dependency. This takes the specification for the dependency and the optional flag.
   *
   * @param spec The specification for the dependency.
   * @param optional The optional flag.
   * @return The dependency object.
   * @see Dependency#Dependency(String, boolean)
   */
  public Dependency dependency(String spec, boolean optional) {
    Dependency dependency = new Dependency(spec, optional);
    group.dependencies.add(dependency);
    return dependency;
  }
}
