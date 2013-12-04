/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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

import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.workflow.Workflow;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the project.
 *
 * @author Brian Pontarelli
 */
public class Project {
  public final Map<String, Target> targets = new HashMap<>();

  public Path directory;

  public String group;

  public String name;

  public Version version;

  public Workflow workflow;
}
