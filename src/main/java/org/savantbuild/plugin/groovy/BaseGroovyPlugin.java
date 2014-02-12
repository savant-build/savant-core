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
package org.savantbuild.plugin.groovy;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.savantbuild.dep.domain.ArtifactID;
import org.savantbuild.domain.Project;
import org.savantbuild.output.Output;
import org.savantbuild.plugin.Plugin;
import org.savantbuild.runtime.BuildFailureException;

import groovy.lang.GroovyObjectSupport;

/**
 * Abstract base class for plugins. This defines some helper methods that all plugins will need and it also allows
 * Savant to pass the {@link Project} and {@link Output} objects to plugins.
 *
 * @author Brian Pontarelli
 */
public class BaseGroovyPlugin extends GroovyObjectSupport implements Plugin {
  public final Output output;

  public final Project project;

  protected BaseGroovyPlugin(Project project, Output output) {
    this.project = project;
    this.output = output;
  }

  /**
   * Fails the build with the given message by throwing a {@link BuildFailureException}.
   *
   * @param message The failure message.
   */
  protected void fail(String message, Object... values) {
    output.error(message, values);
    throw new BuildFailureException(message);
  }

  /**
   * Loads the plugin configuration file. If the configuration file doesn't exist, this throws an exception.
   * <p>
   * The configuration file location is standardized and based on the ArtifactID of the plugin and the user's home
   * directory.
   * <p>
   * The location pattern is as follows:
   * <p>
   * <pre>
   *   &lt;user.home>/plugins/&lt;id.group>.&lt;id.name>.properties
   * </pre>
   *
   * @param id           The artifact Id that is used to load the configuration file.
   * @param errorMessage The error message to print out if the configuration file doesn't exist or is invalid.
   * @return The Properties object for the configuration file.
   */
  protected Properties loadConfiguration(ArtifactID id, String errorMessage) {
    Path configFile = project.pluginConfigurationDirectory.resolve(id.group + "." + id.name + ".properties");
    if (!Files.isRegularFile(configFile)) {
      fail(errorMessage);
    }

    try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
      Properties properties = new Properties();
      properties.load(fis);
      return properties;
    } catch (IOException e) {
      throw new BuildFailureException("Failed to load the plugin configuration file [" + configFile + "]", e);
    }
  }
}
