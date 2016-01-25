/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.diagram.algorithm.orthogonal;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DivergePointsFinderTest {
  private Set<List<Vector>> lines = new HashSet<>();

  @Test
  public void sameLines() {
    List<Vector> l1 = create();
    l1.add(new Vector(0, 0));
    l1.add(new Vector(10, 0));
    l1.add(new Vector(10, 10));
    List<Vector> l2 = create();
    l2.add(new Vector(0, 0));
    l2.add(new Vector(10, 0));
    l2.add(new Vector(10, 10));

    List<Vector> points = new DivergePointsFinder().find(lines);
    assertTrue(points.size() == 0);
  }

  @Test
  public void divergeAtPoint() {
    List<Vector> l1 = create();
    l1.add(new Vector(0, 0));
    l1.add(new Vector(10, 0));
    l1.add(new Vector(10, 10));
    List<Vector> l2 = create();
    l2.add(new Vector(0, 0));
    l2.add(new Vector(10, 0));
    l2.add(new Vector(10, -10));

    List<Vector> points = new DivergePointsFinder().find(lines);
    assertTrue(points.size() == 1);
    assertEquals(new Vector(10, 0), points.get(0));
  }

  @Test
  public void divergeInMiddle() {
    List<Vector> l1 = create();
    l1.add(new Vector(0, 0));
    l1.add(new Vector(10, 0));
    l1.add(new Vector(10, 10));
    l1.add(new Vector(20, 10));
    List<Vector> l2 = create();
    l2.add(new Vector(0, 0));
    l2.add(new Vector(10, 0));
    l2.add(new Vector(10, 20));
    l2.add(new Vector(20, 20));

    List<Vector> points = new DivergePointsFinder().find(lines);
    assertTrue(points.size() == 1);
    assertEquals(new Vector(10, 10), points.get(0));
  }

  private List<Vector> create() {
    List<Vector> list = new ArrayList<>();
    lines.add(list);
    return list;
  }
}