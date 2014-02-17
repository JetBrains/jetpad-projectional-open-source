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

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public class IntGeomUtil {
  public static boolean intersects(Segment s1, Segment s2) {
    return s2.intersection(s1) != null || endsIntersection(s1, s2) != null;
  }

  public static Vector findAxisIntersection(Segment s1, Segment s2) {
    Vector v = endsIntersection(s1, s2);
    if (v != null) {
      return v;
    } else {
      return createAxis(s1).add(createAxis(s2));
    }
  }

  public static Rectangle increase(Rectangle rect, int increaseSize) {
    Vector newOrigin = rect.origin.sub(new Vector(increaseSize / 2, increaseSize / 2));
    Vector newDimension = rect.dimension.add(new Vector(increaseSize, increaseSize));
    return new Rectangle(newOrigin, newDimension);
  }

  private static Vector endsIntersection(Segment s1, Segment s2) {
    if (s1.contains(s2.start)) return s2.start;
    if (s1.contains(s2.end)) return s2.end;
    if (s2.contains(s1.start)) return s1.start;
    if (s2.contains(s1.end)) return s1.end;
    return null;
  }

  private static Vector createAxis(Segment s) {
    return new Vector(s.start.x == s.end.x ? s.start.x : 0, s.start.y == s.end.y ? s.start.y : 0);
  }


  public static List<Segment> getSegments(List<Vector> path) {
    List<Segment> segments = new ArrayList<Segment>(path.size() - 1);
    Vector prev = null;
    for (Vector v: path) {
      if (prev != null) {
        Segment s = new Segment(prev, v);
        segments.add(s);
      }
      prev = v;
    }
    return segments;
  }
}