/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.diagram.view.block;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.base.GridDirection;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BlockViewPortLayouterTest {
  private List<Vector> dim = new ArrayList<>();
  private BlockViewPortLayouter layouter = new BlockViewPortLayouter(new Rectangle(100, 100, 100, 100));

  @Test
  public void onePortRight() {
    addDim(20, 20);

    List<Vector> o = layouter.layoutPorts(dim, GridDirection.RIGHT);

    assertTrue(o.size() == 1);
    assertEquals(new Vector(200, 140), o.get(0));
  }

  @Test
  public void onePortDown() {
    addDim(20, 20);

    List<Vector> o = layouter.layoutPorts(dim, GridDirection.DOWN);

    assertTrue(o.size() == 1);
    assertEquals(new Vector(140, 80), o.get(0));
  }

  @Test
  public void onePortLeft() {
    addDim(20, 20);

    List<Vector> o = layouter.layoutPorts(dim, GridDirection.LEFT);

    assertTrue(o.size() == 1);
    assertEquals(new Vector(80, 140), o.get(0));
  }

  @Test
  public void onePortUp() {
    addDim(20, 20);

    List<Vector> o = layouter.layoutPorts(dim, GridDirection.UP);

    assertTrue(o.size() == 1);
    assertEquals(new Vector(140, 200), o.get(0));
  }

  @Test
   public void twoPortRight() {
    addDim(20, 20);
    addDim(20, 20);
    List<Vector> o = layouter.layoutPorts(dim, GridDirection.RIGHT);

    assertTrue(o.size() == 2);
    assertEquals(new Vector(200, 100), o.get(0));
    assertEquals(new Vector(200, 180), o.get(1));
  }

  @Test
  public void twoPortDown() {
    addDim(20, 20);
    addDim(20, 20);
    List<Vector> o = layouter.layoutPorts(dim, GridDirection.DOWN);

    assertTrue(o.size() == 2);
    assertEquals(new Vector(100, 80), o.get(0));
    assertEquals(new Vector(180, 80), o.get(1));
  }

  @Test
  public void twoPortLeft() {
    addDim(20, 20);
    addDim(20, 20);
    List<Vector> o = layouter.layoutPorts(dim, GridDirection.LEFT);

    assertTrue(o.size() == 2);
    assertEquals(new Vector(80, 180), o.get(0));
    assertEquals(new Vector(80, 100), o.get(1));
  }

  @Test
  public void twoPortUp() {
    addDim(20, 20);
    addDim(20, 20);
    List<Vector> o = layouter.layoutPorts(dim, GridDirection.UP);

    assertTrue(o.size() == 2);
    assertEquals(new Vector(180, 200), o.get(0));
    assertEquals(new Vector(100, 200), o.get(1));
  }

  @Test
  public void oddRectDimensionLeftRightCorresponds() {
    addDim(20, 20);
    layouter = new BlockViewPortLayouter(new Rectangle(100, 100, 75, 75));
    List<Vector> right = layouter.layoutPorts(dim, GridDirection.RIGHT);
    List<Vector> left = layouter.layoutPorts(dim, GridDirection.LEFT);

    assertTrue(right.get(0).y == left.get(0).y);
  }

  @Test
  public void oddRectDimensionUpDownCorresponds() {
    addDim(20, 20);
    layouter = new BlockViewPortLayouter(new Rectangle(100, 100, 75, 75));
    List<Vector> right = layouter.layoutPorts(dim, GridDirection.UP);
    List<Vector> left = layouter.layoutPorts(dim, GridDirection.DOWN);

    assertTrue(right.get(0).x == left.get(0).x);
  }

  private void addDim(int x, int y) {
    dim.add(new Vector(x, y));
  }
}