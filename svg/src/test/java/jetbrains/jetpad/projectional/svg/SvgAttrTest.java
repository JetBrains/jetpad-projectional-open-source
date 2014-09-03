/*
 * Copyright 2012-2014 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.projectional.svg;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SvgAttrTest {
  SvgEllipseElement element;

  @Before
  public void setUp() {
    element = new SvgEllipseElement(10., 20., 30., 40.);
  }

  @Test
  public void testSetAttr() {
    element.cx().set(100.);
    element.getAttr("fill").set("yellow");
    element.setAttr("stroke", "black");

    assertTrue(element.cx().get().equals(100.));
    assertTrue(element.getAttr("fill").get().equals("yellow"));
    assertTrue(element.getAttr("stroke").get().equals("black"));
  }

  @Test
  public void testNotSetAttr() {
    assertTrue(element.getAttr("fill").get() == null);
  }

  @Test
  public void testResetAttr() {
    element.setAttr("fill", "yellow");
    element.getAttr("fill").set("red");

    assertTrue(element.getAttr("fill").get().equals("red"));
  }
}