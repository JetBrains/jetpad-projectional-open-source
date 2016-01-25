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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;

public class PolyLineView extends MultiPointView {

  @Override
  protected boolean contains(Vector loc) {
    for (int i = 1; i < points.size(); i++) {
      Segment seg = new Segment(points.get(i - 1), points.get(i));
      double distance = seg.distance(loc);
      if (bounds().get().contains(loc) && distance < LineView.THRESHOLD) {
        return true;
      }
    }
    return false;
  }
}