/*
 * Copyright (c) 2022-2025, Inversoft Inc., All Rights Reserved
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

import org.savantbuild.domain.Version;
import org.savantbuild.parser.ParseException;

/**
 * Groovy delegate that captures the semantic version mappings for a project.
 *
 * @author Brian Pontarelli
 */
public class SemanticVersionDelegate {
  public final Map<String, Version> mapping;

  public final Map<String, String> rangeMapping;

  public SemanticVersionDelegate(Map<String, Version> mapping, Map<String, String> rangeMapping) {
    this.mapping = mapping;
    this.rangeMapping = rangeMapping;
  }

  public void mapping(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "id", "version")) {
      throw new ParseException("Invalid mapping definition. It must have an [id] and a [version] attribute like this:\n\n" +
          "  mapping(id: \"org.badver:badver:1.0.0.Final\", version: \"1.0.0\")");
    }

    String id = GroovyTools.toString(attributes, "id");
    String version = GroovyTools.toString(attributes, "version");
    mapping.put(id, new Version(version));
  }

  public void rangeMapping(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "id", "version")) {
      throw new ParseException("Invalid mapping definition. It must have an [id] and a [version] attribute like this:\n\n" +
          "  mapping(id: \"org.range:mc-range-face:[1.0,2.0)\", version: \"1.0\")");
    }

    String id = GroovyTools.toString(attributes, "id");
    String version = GroovyTools.toString(attributes, "version");
    rangeMapping.put(id, version);
  }
}
