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

import java.util.Map;

import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.dep.domain.ArtifactMetaData;
import org.savantbuild.dep.domain.Publication;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publications;
import org.savantbuild.parser.ParseException;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

/**
 * Groovy delegate for defining publications.
 *
 * @author Brian Pontarelli
 */
public class PublicationsDelegate extends GroovyObjectSupport {
  private final Project project;

  private final Publications publications;

  public PublicationsDelegate(Project project, Publications publications) {
    this.project = project;
    this.publications = publications;
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    if (!(args instanceof Object[]) || !(((Object[]) args)[0] instanceof Closure)) {
      throw new ParseException("Invalid publication group definition. It must have the name like this:\n\n" +
          "  publications {\n" +
          "    main {\n" +
          "      ...\n" +
          "    }\n" +
          "  }");
    }

    Closure closure = (Closure) ((Object[]) args)[0];
    closure.setDelegate(new PublicationGroupDelegate(project, publications, name));
    closure.run();
    return publications.publicationGroups.get(name);
  }

  /**
   * Delegate for a publication group.
   *
   * @author Brian Pontarelli
   */
  public static class PublicationGroupDelegate {
    public final Project project;
    public final Publications publications;
    public final String group;

    public PublicationGroupDelegate(Project project, Publications publications, String group) {
      this.project = project;
      this.publications = publications;
      this.group = group;
    }

    /**
     * Defines a publication. This takes a Map of attributes but only the {@code name}, {@code type}, {@code file} and
     * {@code source} attributes are required.
     *
     * @param attributes The attributes.
     * @return The Publication object.
     */
    public Publication publication(Map<String, Object> attributes) {

      if (!GroovyTools.hasAttributes(attributes, "name", "type", "file", "source")) {
        throw new ParseException("Invalid publication definition. It must have the name, type, file and source attributes " +
            "like this:\n\n" +
            "  publication(name: \"foo\", type: \"jar\", file: \"build/jars/foo-${project.version}.jar\", source: \"build/jars/foo-${project.version}-src.jar\")");
      }

      String name = GroovyTools.toString(attributes, "name");
      String type = GroovyTools.toString(attributes, "type");
      String file = GroovyTools.toString(attributes, "file");
      String source = GroovyTools.toString(attributes, "source");
      Artifact artifact = new Artifact(new ArtifactID(project.group, project.name, name, type), project.version, project.license);
      ArtifactMetaData amd = new ArtifactMetaData(project.dependencies, project.license);
      Publication publication = new Publication(artifact, amd, project.directory.resolve(file), project.directory.resolve(source));
      this.publications.add(group, publication);
      return publication;
    }
  }
}
