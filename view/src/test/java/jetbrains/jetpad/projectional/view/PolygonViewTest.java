/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolygonViewTest {
  private PolygonView polygonView = new PolygonView();

  @Before
  public void init() {
    polygonView.points.addAll(Arrays.asList(new Vector(0, 0), new Vector(100, 0), new Vector(100, 100), new Vector(50, 50), new Vector(0, 100)));
  }

  @Test
  public void containsWorks() {
    assertFalse(polygonView.contains(new Vector(-100, 100)));
    assertFalse(polygonView.contains(new Vector(101, 101)));
    assertTrue(polygonView.contains(new Vector(5, 5)));
    assertFalse(polygonView.contains(new Vector(50, 100)));
  }
}