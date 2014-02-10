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
package jetbrains.jetpad.projectional.diagram.algorithm.geom;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AvoidObstacles {
  public Rectangle findNewPosition(Rectangle rect, List<Rectangle> obstacles) {
    List<Vector> horObs = new ArrayList<>();
    List<Vector> verObs = new ArrayList<>();
    for (Rectangle o: obstacles) {
      if (o.innerIntersects(rect)) {
        horObs.add(new Vector(o.origin.x, o.origin.add(o.dimension).x));
        verObs.add(new Vector(o.origin.y, o.origin.add(o.dimension).y));
      }
    }
    if (horObs.size() == 0) {
      return rect;
    }

    Vector min = new Vector(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);
    Vector max = new Vector(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
    horObs.add(min);
    horObs.add(max);
    verObs.add(min);
    verObs.add(max);

    Comparator<Vector> comparator = new Comparator<Vector>() {
      @Override
      public int compare(Vector v1, Vector v2) {
        if (v1.x < v2.x) return -1;
        if (v1.x > v2.x) return 1;
        return 0;
      }
    };
    Collections.sort(horObs, comparator);
    int bestHor = findShift(new Vector(rect.origin.x, rect.origin.x + rect.dimension.x), horObs);
    int bestVer = findShift(new Vector(rect.origin.y, rect.origin.y + rect.dimension.y), verObs);
    Vector shift;
    if (Math.abs(bestHor) < Math.abs(bestVer)) {
      shift = new Vector(bestHor, 0);
    } else {
      shift = new Vector(0, bestVer);
    }
    return rect.add(shift);
  }

  public int findShift(Vector v, List<Vector> obs) {
    Comparator<Vector> comparator = new Comparator<Vector>() {
      @Override
      public int compare(Vector v1, Vector v2) {
        if (v1.x < v2.x) return -1;
        if (v1.x > v2.x) return 1;
        return 0;
      }
    };
    Collections.sort(obs, comparator);
    int res1 = findShift(v.y - v.x, v.x, obs);

    List<Vector> invObs = new ArrayList<>(obs.size());
    for (Vector o: obs) {
      invObs.add(new Vector(-o.y, -o.x));
    }
    Collections.sort(invObs, comparator);

    int res2 = -findShift(v.y - v.x, -v.y, invObs);
    if (Math.abs(res1) < Math.abs(res2)) {
      return res1;
    } else {
      return res2;
    }
  }

  private int findShift(int len, int pos, List<Vector> obs) {
    Vector cur = obs.get(0);
    int best = Integer.MAX_VALUE;
    for (Vector v: obs) {
      if (v.y <= cur.y) continue;
      if (v.x >= cur.y + len) {
        if (Math.abs(cur.y - pos) < Math.abs(best)) {
          best = cur.y - pos;
        }
      }
      cur = v;
    }
    return best;
  }
}