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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Groovy helpers.
 *
 * @author Brian Pontarelli
 */
public class GroovyTools {
  /**
   * Safely converts an attribute to a String.
   *
   * @param attributes The attributes.
   * @param key        The key of the attribute to convert.
   * @return Null if the object is null, otherwise the result of calling toString.
   */
  public static String toString(Map<String, Object> attributes, String key) {
    if (attributes == null) {
      return null;
    }

    Object object = attributes.get(key);
    if (object == null) {
      return null;
    }

    return object.toString();
  }

  /**
   * Converts the object to a List of Strings. If the object is a List, this converts all non-null items to Strings and
   * deletes null values. If it isn't a List, it returns a single element List with the value of object.toString().
   *
   * @param value The value to convert.
   * @return The List of Strings.
   */
  @SuppressWarnings("unchecked")
  public static List<String> toListOfStrings(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof List) {
      List<Object> list = (List<Object>) value;
      return list.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
    }

    return asList(value.toString());
  }

  /**
   * Checks if the given attributes Map has all of the given keys. The values for the keys must be non-null and
   * non-empty.
   *
   * @param attributes The attributes map.
   * @param keys The keys.
   * @return True if the map contains all of the keys, false otherwise.
   */
  public static boolean hasAttributes(Map<String, Object> attributes, String... keys) {
    for (String key : keys) {
      Object value = attributes.get(key);
      if (value == null || (value instanceof CharSequence && value.toString().trim().length() == 0)) {
        return false;
      }
    }

    return true;
  }
}
