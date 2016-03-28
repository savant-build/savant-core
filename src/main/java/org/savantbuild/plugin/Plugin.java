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
package org.savantbuild.plugin;

import org.savantbuild.domain.Project;
import org.savantbuild.output.Output;
import org.savantbuild.runtime.RuntimeConfiguration;

/**
 * <p>
 * Defines a Plugin for the Savant build system. This is a marker interface that allows Plugins to be written in any
 * language. The only requirements of Plugins is that they must have a single constructor that takes a {@link Project},
 * {@link RuntimeConfiguration} and a {@link Output} (in that order). For example:
 * </p>
 * <pre>
 *   class GroovyPlugin {
 *     GroovyPlugin(Project project, RuntimeConfiguration runtimeConfiguration, Output output) {
 *       ...
 *     }
 *   }
 * </pre>
 *
 * @author Brian Pontarelli
 */
public interface Plugin {
}
