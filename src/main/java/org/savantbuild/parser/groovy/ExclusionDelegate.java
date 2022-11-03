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
package org.savantbuild.parser.groovy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.parser.ParseException;

/**
 * Groovy delegate that defines the exclusions for a single dependency.
 *
 * @author Brian Pontarelli
 */
public class ExclusionDelegate {
  private final List<ArtifactID> exclusions = new ArrayList<>();

  /**
   * Defines am exclusion. This takes a Map of attributes but only the {@code id} attributes is required. This attribute
   * defines the exclusion using the shorthand notation.
   *
   * @param attributes The attributes.
   * @return Nothing
   * @see Artifact#Artifact(String, boolean)
   */
  public ArtifactID exclusion(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "id")) {
      throw new ParseException("Invalid exclusion definition. It must have the id attribute like this:\n\n" +
          "  exclusion(id: \"org.example:foo\")");
    }

    String id = GroovyTools.toString(attributes, "id");
    ArtifactID exclusion = new ArtifactID(id);
    exclusions.add(exclusion);
    return exclusion;
  }

  public List<ArtifactID> getExclusions() {
    return exclusions;
  }
}
