/*
 * Copyright (c) 2014-2024, Inversoft Inc., All Rights Reserved
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.dep.domain.ArtifactSpec;
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
public class DependencyDelegate {
  private final DependencyGroup group;

  private final Map<String, Version> semanticVersionMappings;

  private Map<String, String> reverseSemanticMappings;

  public DependencyDelegate(DependencyGroup group,
                            Map<String, Version> semanticVersionMappings) {
    this.group = group;
    this.semanticVersionMappings = semanticVersionMappings;
  }

  /**
   * Defines a dependency without exclusion by calling {@link #dependency(Map, Closure)} with null for the closure.
   *
   * @param attributes The attributes.
   * @return The dependency object.
   * @see Artifact#Artifact(String)
   */
  public Artifact dependency(Map<String, Object> attributes) {
    return dependency(attributes, null);
  }

  /**
   * Defines a dependency. This takes a Map of attributes but only the {@code id} attributes is required. This attribute
   * defines the dependency (as a String). The {@code optional} attribute is optional and defines if the dependency is
   * optional.
   *
   * @param attributes The attributes.
   * @param closure    The exclusion delegate if one was provided.
   * @return The dependency object.
   * @see Artifact#Artifact(String)
   */
  public Artifact dependency(Map<String, Object> attributes, @DelegatesTo(ExclusionDelegate.class) Closure<?> closure) {
    if (!GroovyTools.hasAttributes(attributes, "id")) {
      throw new ParseException("Invalid dependency definition. It must have the id attribute like this:\n\n" +
          "  dependency(id: \"org.example:foo:0.1.0\", optional: false)");
    }

    List<ArtifactID> exclusions = null;
    if (closure != null) {
      ExclusionDelegate delegate = new ExclusionDelegate();
      closure.setDelegate(delegate);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.run();
      exclusions = delegate.getExclusions();
    }

    String id = GroovyTools.toString(attributes, "id");
    boolean skipCompatibilityCheck = attributes.containsKey("skipCompatibilityCheck") ? (Boolean) attributes.get("skipCompatibilityCheck") : false;
    buildReverseSemanticMappings();
    var nonSemanticVersion = reverseSemanticMappings.get(id);
    Artifact dependency = new Artifact(id, nonSemanticVersion, skipCompatibilityCheck, exclusions);
    group.dependencies.add(dependency);
    return dependency;
  }

  private void buildReverseSemanticMappings() {
    if (reverseSemanticMappings != null) {
      return;
    }

    // we want the semanticVersions/DSL to function the same way in all cases
    // meaning mapping(id: "net.sf.saxon:Saxon-HE:10.9", version: "10.9.0")
    // where 10.9 is the bad version

    // and we want the actual dependency specs to be "good" versions, e.g.
    // dependency(id: "net.sf.saxon:Saxon-HE:10.9.0")

    // therefore, when we are handling a direct dependency, we have to swap our mapping
    // and use the good versions as the keys and the bad versions as the values
    Function<String, String> getWithoutVersion = artifact -> {
      var id = new ArtifactSpec(artifact, false).id;
      return id.group + ":" + id.name;
    };
    Function<String, String> getVersion = artifact -> {
      var withoutVersion = getWithoutVersion.apply(artifact);
      return artifact.replace(withoutVersion + ":", "");
    };

    reverseSemanticMappings = semanticVersionMappings.entrySet()
                                                     .stream()
                                                     .collect(Collectors.toMap(kv -> getWithoutVersion.apply(kv.getKey()) + ":" + kv.getValue().toString(),
                                                         kv -> getVersion.apply(kv.getKey())));
  }
}
