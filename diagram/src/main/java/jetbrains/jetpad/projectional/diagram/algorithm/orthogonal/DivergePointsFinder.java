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

import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;

import java.util.*;

public class DivergePointsFinder {
  public List<Vector> find(Set<List<Vector>> lines) {
    List<Vector> divergePoints = new ArrayList<Vector>();

    Map<Vector, List<Vector>> directions = new HashMap<Vector, List<Vector>>();
    for (List<Vector> line: lines) {
      Vector prev = null;
      for (Vector p: line) {
        if (prev != null) {
          if (!directions.containsKey(prev)) {
            directions.put(prev, new ArrayList<Vector>());
          }
          directions.get(prev).add(p.sub(prev));
        }
        prev = p;
      }
    }
    for (List<Vector> line: lines) {
      Vector prev = null;
      for (Vector p: line) {
        if (prev != null) {
          Segment s = new Segment(prev, p);
          for (Vector dirP: directions.keySet()) {
            if (p.equals(dirP) || prev.equals(dirP)) continue;
            if (s.contains(dirP)) {
              directions.get(dirP).add(p.sub(dirP));
            }
          }
        }
        prev = p;
      }
    }

    for (Vector p: directions.keySet()) {
      List<Vector> dirs = directions.get(p);
      boolean isDevergePoint = false;
      for (int i = 0; i < dirs.size(); i++) {
        for (int j = i + 1; j < dirs.size(); j++) {
          Vector v1 = dirs.get(i);
          Vector v2 = dirs.get(j);
          isDevergePoint |= !(v1.isParallel(v2) && v1.dotProduct(v2) > 0);
        }
      }
      if (isDevergePoint) {
        divergePoints.add(p);
      }
    }

    return divergePoints;
  }
}