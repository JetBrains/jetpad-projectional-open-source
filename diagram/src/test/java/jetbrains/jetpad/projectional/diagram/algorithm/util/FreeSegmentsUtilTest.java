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
package jetbrains.jetpad.projectional.diagram.algorithm.util;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FreeSegmentsUtilTest {
  @Test
  public void notIntersecting() {
    List<Vector> seg = new ArrayList<>();
    seg.add(new Vector(0, 10));

    List<Vector> res = FreeSegmentsUtil.removeSegment(seg, new Vector(-10, 0));
    assertTrue(res.size() == 1);
    assertEquals(new Vector(0, 10), res.get(0));

    res = FreeSegmentsUtil.removeSegment(seg, new Vector(10, 100));
    assertTrue(res.size() == 1);
    assertEquals(new Vector(0, 10), res.get(0));
  }

  @Test
  public void occupiedPoint() {
    List<Vector> seg = new ArrayList<>();
    seg.add(new Vector(0, 10));

    List<Vector> res = FreeSegmentsUtil.removeSegment(seg, new Vector(5, 5));
    assertTrue(res.size() == 2);
    assertEquals(new Vector(0, 5), res.get(0));
    assertEquals(new Vector(5, 10), res.get(1));
  }

  @Test
  public void occupiedIntersectsMany() {
    List<Vector> seg = new ArrayList<>();
    seg.add(new Vector(0, 2));
    seg.add(new Vector(4, 6));
    seg.add(new Vector(8, 10));
    List<Vector> res = FreeSegmentsUtil.removeSegment(seg, new Vector(1, 9));
    assertTrue(res.size() == 2);
    assertEquals(new Vector(0, 1), res.get(0));
    assertEquals(new Vector(9, 10), res.get(1));
  }
}