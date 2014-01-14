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
package org.savantbuild.runtime;

import org.savantbuild.BaseUnitTest;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the default runtime configuration parser.
 *
 * @author Brian Pontarelli
 */
public class DefaultRuntimeConfigurationParserTest extends BaseUnitTest {
  @Test
  public void parse() throws Exception {
    DefaultRuntimeConfigurationParser parser = new DefaultRuntimeConfigurationParser();
    RuntimeConfiguration config = parser.parse("foo", "bar");
    assertTrue(config.colorizeOutput);
    assertEquals(config.targets, asList("foo", "bar"));

    config = parser.parse("foo", "bar", "--noColor");
    assertFalse(config.colorizeOutput);
    assertEquals(config.targets, asList("foo", "bar"));

    config = parser.parse("--noColor", "foo", "bar");
    assertFalse(config.colorizeOutput);
    assertEquals(config.targets, asList("foo", "bar"));

    config = parser.parse("foo", "--noColor", "bar");
    assertFalse(config.colorizeOutput);
    assertEquals(config.targets, asList("foo", "bar"));
  }
}
