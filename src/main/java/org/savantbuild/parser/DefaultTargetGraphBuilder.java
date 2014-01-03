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

import org.savantbuild.util.Graph;
import org.savantbuild.util.HashGraph;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;

/**
 * Default target graph builder.
 *
 * @author Brian Pontarelli
 */
public class DefaultTargetGraphBuilder implements TargetGraphBuilder {
  @Override
  public Graph<Target, Object> build(Project project) {
    Graph<Target, Object> graph = new HashGraph<>();
    project.targets.forEach((name, target) -> {
      if (target.dependencies == null) {
        return;
      }

      target.dependencies.forEach((dependency) -> {
        Target dependencyTarget = project.targets.get(dependency);
        if (dependencyTarget == null) {
          throw new ParseException("Invalid dependsOn for target [" + name + "]. Target [" + dependency + "] does not exist");
        }

        graph.addEdge(target, dependencyTarget, Project.GRAPH_EDGE);
      });
    });

    return graph;
  }
}
