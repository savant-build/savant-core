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

import org.savantbuild.dep.ArtifactTools;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.dep.domain.ArtifactSpec;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.domain.Version;
import org.savantbuild.domain.VersionException;
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

  public DependencyDelegate(DependencyGroup group, Map<String, Version> semanticVersionMappings) {
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
      throw new ParseException("""
          Invalid dependency definition. It must have the id attribute like this:

            dependency(id: "org.example:foo:0.1.0", optional: false)
          """);
    }

    List<ArtifactID> exclusions = null;
    if (closure != null) {
      ExclusionDelegate delegate = new ExclusionDelegate();
      closure.setDelegate(delegate);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.run();
      exclusions = delegate.getExclusions();
    }

    boolean skipCompatibilityCheck = attributes.containsKey("skipCompatibilityCheck") ? (Boolean) attributes.get("skipCompatibilityCheck") : false;
    String id = GroovyTools.toString(attributes, "id");
    ArtifactSpec spec = new ArtifactSpec(id);
    try {
      Version version = ArtifactTools.determineSemanticVersion(spec, semanticVersionMappings);

      // Double check if the original was a short version (i.e. 1.0) and the ArtifactTools method fixed it to semantic (i.e. 1.0.0).
      // In this case, the original is non-semantic, and we need to save it so that we can fetch it from Maven.
      String nonSemanticVersion = null;
      if (!version.toString().equals(spec.version)) {
        nonSemanticVersion = spec.version;
      }

      Artifact dependency = new Artifact(spec.id, version, nonSemanticVersion, skipCompatibilityCheck, exclusions);
      group.dependencies.add(dependency);
      return dependency;
    } catch (VersionException e) {
      throw new ParseException("Invalid dependency definition [" + id + "]. This dependency has an invalid version and caused this error:\n\n\t" + e.getMessage().replace("\n", "\n\t"));
    }
  }
}
