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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.savantbuild.runtime.BuildFailureException;

import groovy.lang.GroovyObjectSupport;

/**
 * This class loads an optional global configuration file named config.properties in the ~/.savant/ directory. This is a
 * dynamic Groovy object that fails if lookups fail. This ensures that values from the configuration that the project
 * depends on exist.
 *
 * @author Brian Pontarelli
 */
public class GlobalConfiguration extends GroovyObjectSupport {
  public final Properties properties = new Properties();

  public GlobalConfiguration() {
    Path configFile = Paths.get(System.getProperty("user.home"), ".savant/config.properties");
    if (Files.isRegularFile(configFile)) {
      try (InputStream is = Files.newInputStream(configFile)) {
        properties.load(is);
      } catch (IOException e) {
        throw new BuildFailureException("Unable to load global configuration file ~/.savant/config.properties", e);
      }
    }
  }

  @Override
  public Object getProperty(String property) {
    String value = properties.getProperty(property);
    if (value == null) {
      throw new BuildFailureException("Missing global configuration property [" + property + "]. You must define this " +
          "property in the global configuration file ~/.savant/config.properties");
    }

    return value;
  }

  @Override
  public void setProperty(String property, Object newValue) {
    throw new BuildFailureException("You attempted to set the property [" + property + "] to the value [" + newValue +
        "]. You cannot set/change global configuration properties from a build file.");
  }
}
