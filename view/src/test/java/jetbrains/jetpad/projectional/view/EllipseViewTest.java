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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EllipseViewTest {

  @Test
  public void simpleContains() {

    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(50, 10));
    ellipse.validate();

    assertTrue(ellipse.contains(new Vector(0, 0)));
    assertTrue(ellipse.contains(new Vector(50, 0)));
    assertTrue(ellipse.contains(new Vector(0, 10)));

    assertFalse(ellipse.contains(new Vector(50, 10)));
  }

  @Test
  public void containsOverflow() {

    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(5000, 1000));
    ellipse.validate();

    assertTrue(ellipse.contains(new Vector(0, 0)));
    assertTrue(ellipse.contains(new Vector(5000, 0)));
    assertTrue(ellipse.contains(new Vector(0, 1000)));

    assertFalse(ellipse.contains(new Vector(5000, 1000)));
  }

  @Test
  public void simpleSectorContains() {
    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(10, 10));
    ellipse.from().set(Math.PI / 4);
    ellipse.to().set(Math.PI * 3.0 / 4.0);

    assertFalse(ellipse.contains(new Vector(0, 5)));
    assertTrue(ellipse.contains(new Vector(0, -5)));
    assertFalse(ellipse.contains(new Vector(-5, 0)));
    assertFalse(ellipse.contains(new Vector(5, 0)));
  }

  @Test
  public void incorrectPhiContainsBug() {
    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(10, 10));
    ellipse.from().set(0.6 * (2 * Math.PI));
    ellipse.to().set(0.8 * (2 * Math.PI));
    ellipse.validate();

    assertTrue(ellipse.contains(new Vector(-1, 1)));
    assertFalse(ellipse.contains(new Vector(-1, -1)));
  }
}