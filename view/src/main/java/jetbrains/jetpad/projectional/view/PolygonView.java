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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public class PolygonView extends MultiPointView {
  @Override
  protected boolean contains(Vector loc) {
    List<Segment> segments = getSegments();

    Rectangle bounds = calculateBounds();
    Segment outSegment = new Segment(loc, new Vector(bounds.origin.x + bounds.dimension.x * 2, 0));
    int intersections = 0;

    for (Segment s : segments) {
      if (outSegment.intersection(s) != null) {
        intersections++;
      }
    }

    return intersections % 2 == 1;
  }

  private List<Segment> getSegments() {
    List<Segment> segments = new ArrayList<>();
    int n = points.size();

    for (int i = 1; i < n; i++) {
      segments.add(new Segment(points.get(i - 1), points.get(i)));
    }

    if (!points.isEmpty()) {
      segments.add(new Segment(points.get(n - 1), points.get(0)));
    }
    return segments;
  }
}