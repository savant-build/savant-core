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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import groovy.lang.GString;
import static java.util.Arrays.asList;

/**
 * Groovy helpers.
 *
 * @author Brian Pontarelli
 */
public class GroovyTools {
  /**
   * Ensures that the attributes are valid. This checks if the attributes are null and there are no required attributes.
   * If this is the case, it returns true. Otherwise, the attributes must be a Map and must contain the required
   * attributes and have the correct attribute types.
   *
   * @param attributes         The attributes object.
   * @param possibleAttributes The list of possible attributes. If an attributes is specified that doesn't exist in this
   *                           Collection, false will be returned.
   * @param requiredAttributes A list of required attributes.
   * @param types              The attribute types.
   * @return True if the attributes are valid, false otherwise.
   */
  public static boolean attributesValid(Map<String, Object> attributes, Collection<String> possibleAttributes,
                                        Collection<String> requiredAttributes, Map<String, Class<?>> types) {
    if (attributes == null && requiredAttributes.isEmpty()) {
      return true;
    } else if (attributes == null) {
      return false;
    }

    return possibleAttributes.containsAll(attributes.keySet()) && hasAttributes(attributes, requiredAttributes) && hasAttributeTypes(attributes, types);
  }

  /**
   * Checks if the given attributes Map has the correct types. This handles the GString case since that is a Groovy
   * special class that is converted to String dynamically.
   *
   * @param attributes The attributes map.
   * @param types      The types.
   * @return True if the map contains the correct types, false otherwise.
   */
  public static boolean hasAttributeTypes(Map<String, Object> attributes, Map<String, Class<?>> types) {
    if (attributes == null) {
      return false;
    }

    for (String key : types.keySet()) {
      Object value = attributes.get(key);
      if (value == null) {
        continue;
      }

      Class<?> type = types.get(key);
      if (type == String.class && !(value instanceof String || value instanceof GString)) {
        return false;
      } else if (type != String.class && !type.isAssignableFrom(value.getClass())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the given attributes Map has all the given attribute names. The values for the attribute names must be
   * non-null.
   *
   * @param attributes     The attributes map.
   * @param attributeNames The attribute names.
   * @return True if the map contains all the attribute names, false otherwise.
   */
  public static boolean hasAttributes(Map<String, Object> attributes, Iterable<String> attributeNames) {
    if (attributes == null) {
      return false;
    }

    for (String attributeName : attributeNames) {
      Object value = attributes.get(attributeName);
      if (value == null) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the given attributes Map has all the given attribute names. The values for the attribute names must be
   * non-null and non-empty.
   *
   * @param attributes     The attributes map.
   * @param attributeNames The attribute names.
   * @return True if the map contains all the attribute names, false otherwise.
   */
  public static boolean hasAttributes(Map<String, Object> attributes, String... attributeNames) {
    return hasAttributes(attributes, asList(attributeNames));
  }

  /**
   * Puts all the values from the defaults map into the main map if they are absent. This is a good way to set up
   * default values.
   *
   * @param map      The main map.
   * @param defaults The defaults map.
   */
  public static void putDefaults(Map<String, Object> map, Map<String, Object> defaults) {
    defaults.forEach(map::putIfAbsent);
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

    return Collections.singletonList(value.toString());
  }

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
}
