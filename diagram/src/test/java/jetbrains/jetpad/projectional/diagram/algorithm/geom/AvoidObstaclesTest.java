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
package jetbrains.jetpad.projectional.diagram.algorithm.geom;

import jetbrains.jetpad.geometry.Rectangle;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class AvoidObstaclesTest {
  @Test
  public void noObstacles() {
    Rectangle r = new Rectangle(0, 0, 10, 10);
    Rectangle pos = new AvoidObstacles().findNewPosition(r, new ArrayList<Rectangle>());
    assertEquals(r, pos);
  }

  @Test
  public void notIntersecting() {
    Rectangle r = new Rectangle(0, 0, 10, 10);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(-100, 10, 210, 100));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(r, pos);
  }

  @Test
  public void between() {
    Rectangle r = new Rectangle(0, 0, 10, 10);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(-1, 1, 1, 1));
    obs.add(new Rectangle(10, 2, 1, 1));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(r, pos);
  }

  @Test
     public void oneObstacleShiftLeft() {
    Rectangle r = new Rectangle(0, 0, 10, 100);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(-2, 50, 13, 1));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(new Rectangle(11, 0, 10, 100), pos);
  }

  @Test
  public void oneObstacleShiftRight() {
    Rectangle r = new Rectangle(0, 0, 10, 100);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(-1, 50, 13, 1));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(new Rectangle(-11, 0, 10, 100), pos);
  }

  @Test
  public void oneObstacleShiftUp() {
    Rectangle r = new Rectangle(0, 0, 100, 10);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(50, -2, 1, 13));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(new Rectangle(0, 11, 100, 10), pos);
  }

  @Test
  public void oneObstacleShiftDown() {
    Rectangle r = new Rectangle(0, 0, 100, 10);
    ArrayList<Rectangle> obs = new ArrayList<>();
    obs.add(new Rectangle(50, -1, 1, 13));
    Rectangle pos = new AvoidObstacles().findNewPosition(r, obs);
    assertEquals(new Rectangle(0, -11, 100, 10), pos);
  }
}