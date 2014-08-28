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

import org.savantbuild.parser.ParseException;

/**
 * Base delegate that helps with type conversions and such.
 *
 * @author Brian Pontarelli
 */
public class BaseDelegate {
  @SuppressWarnings("unchecked")
  protected Map<String, Object> toAttributes(Object parameter) {
    if (parameter == null) {
      return null;
    }

    if (parameter instanceof Map) {
      return (Map<String, Object>) parameter;
    }

    String lineNumber = null;
    StackTraceElement[] element = Thread.currentThread().getStackTrace();
    for (StackTraceElement stackTraceElement : element) {
      String fileName = stackTraceElement.getFileName();
      int index = fileName.indexOf("build.savant");
      if (index > 0) {
        lineNumber = fileName.substring(index + 1, fileName.length() - 2);
      }
    }

    if (lineNumber == null) {
      throw new ParseException("Invalid declaration in your build file. All statements in Savant use the Groovy named parameter like this:\n\n" +
          "  foo(bar: \"baz\")");
    }

    throw new ParseException("Invalid declaration in your build file at line [" + lineNumber + "]. All statements in Savant use the Groovy named parameter like this:\n\n" +
        "  foo(bar: \"baz\")");
  }
}
