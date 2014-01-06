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
package jetbrains.jetpad.projectional.diagram.algorithm.orthogonal;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrthogonalRouteWithObstaclesTest {
  OrthogonalRouteWithObstacles o = new OrthogonalRouteWithObstacles();
  
  @Test
  public void noObstaclesHorRoute() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    List<Vector> r = o.findRoute(s, e);
    
    assertTrue(r.size() == 2);
    assertEquals(s, r.get(0));
    assertEquals(e, r.get(1));
  }

  @Test
  public void noObstaclesVerRoute() {
    Vector s = new Vector(0, 1);
    Vector e = new Vector(0, 2);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 2);
    assertEquals(s, r.get(0));
    assertEquals(e, r.get(1));
  }

  @Test
  public void noObstaclesPolylineRoute() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 200);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 3);
    assertEquals(s, r.get(0));
    assertEquals(new Vector(100, 200), r.get(1));
    assertEquals(e, r.get(2));
  }

  @Test
  public void ignoreObstacle() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    Rectangle rect = new Rectangle(110, 1, 80, 80);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 2);
    assertEquals(s, r.get(0));
    assertEquals(e, r.get(1));
  }

  @Test
  public void borderObstacle() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    Rectangle rect = new Rectangle(110, 0, 80, 80);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 2);
    assertEquals(s, r.get(0));
    assertEquals(e, r.get(1));
  }

  @Test
  public void bypassObstacle() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    Rectangle rect = new Rectangle(110, -1, 80, 80);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 4);
    assertEquals(s, r.get(0));
    assertEquals(new Vector(100, -1), r.get(1));
    assertEquals(new Vector(200, -1), r.get(2));
    assertEquals(e, r.get(3));
  }

  //@Test
  public void startInsideObstacle() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    Rectangle rect = new Rectangle(99, -1, 2, 2);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 2);
    assertEquals(s, r.get(0));
    assertEquals(e, r.get(1));
  }

  @Test
  public void horForbiddenSegment() {
    Vector s = new Vector(100, 0);
    Vector e = new Vector(200, 0);
    Rectangle rect = new Rectangle(110, -1, 80, 2);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Segment> forbidden = new ArrayList<Segment>();
    forbidden.add(new Segment(new Vector(300, -1), new Vector(-100, -1)));
    o.addForbiddenSegments(forbidden);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 4);
    assertEquals(s, r.get(0));
    assertEquals(new Vector(100, 1), r.get(1));
    assertEquals(new Vector(200, 1), r.get(2));
    assertEquals(e, r.get(3));
  }

  @Test
  public void verForbiddenSegment() {
    Vector s = new Vector(0, 100);
    Vector e = new Vector(0, 200);
    Rectangle rect = new Rectangle(-1, 110, 2, 80);
    List<Rectangle> a = new ArrayList<Rectangle>();
    a.add(rect);
    o.setObstacles(a);
    List<Segment> forbidden = new ArrayList<Segment>();
    forbidden.add(new Segment(new Vector(-1, 300), new Vector(-1, -100)));
    o.addForbiddenSegments(forbidden);
    List<Vector> r = o.findRoute(s, e);

    assertTrue(r.size() == 4);
    assertEquals(s, r.get(0));
    assertEquals(new Vector(1, 100), r.get(1));
    assertEquals(new Vector(1, 200), r.get(2));
    assertEquals(e, r.get(3));
  }

  @Test
  public void startEqualsEnd() {
    Vector s = new Vector(0, 0);
    Vector e = new Vector(0, 0);
    List<Rectangle> a = Arrays.asList(new Rectangle(1, 1, 1, 1));
    List<Vector> r = o.findRoute(s, e);
    assertTrue(r.size() == 1);
    assertEquals(s, r.get(0));
  }
}