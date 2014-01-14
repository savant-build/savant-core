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
package org.savantbuild.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.savantbuild.dep.domain.Publication;

/**
 * Models the publication set in the project build file.
 *
 * @author Brian Pontarelli
 */
public class Publications {
  public final Map<String, List<Publication>> publicationGroups = new HashMap<>();

  /**
   * Adds the given Publication to the given group.
   *
   * @param group       The group.
   * @param publication The Publication.
   * @return This Publications object.
   */
  public Publications add(String group, Publication publication) {
    List<Publication> publications = publicationGroups.get(group);
    if (publications == null) {
      publications = new ArrayList<>();
      publicationGroups.put(group, publications);
    }

    publications.add(publication);
    return this;
  }

  /**
   * @return All of the publications flattened.
   */
  public List<Publication> allPublications() {
    List<Publication> result = new ArrayList<>();
    publicationGroups.forEach((group, list) -> result.addAll(list));
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Publications that = (Publications) o;
    return publicationGroups.equals(that.publicationGroups);
  }

  /**
   * Null safe getter for a single group. This returns the list of publications or an empty list if the publication
   * group is empty.
   *
   * @param group The group.
   * @return The list of publications (never null).
   */
  public List<Publication> group(String group) {
    List<Publication> publications = publicationGroups.get(group);
    if (publications == null) {
      return Collections.emptyList();
    }

    return publications;
  }

  @Override
  public int hashCode() {
    return publicationGroups.hashCode();
  }

  /**
   * @return The total number of Publications.
   */
  public int size() {
    int size = 0;
    for (List<Publication> publications : publicationGroups.values()) {
      size += publications.size();
    }
    return size;
  }
}
