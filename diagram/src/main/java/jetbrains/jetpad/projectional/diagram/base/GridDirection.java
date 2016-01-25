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
package jetbrains.jetpad.projectional.diagram.base;

import jetbrains.jetpad.geometry.Vector;

public enum GridDirection {
  RIGHT(1, 0),
  DOWN(0, -1),
  LEFT(-1, 0),
  UP(0, 1);

  private Vector myDir;

  private GridDirection(int x, int y) {
    myDir = new Vector(x, y);
  }

  public Vector dir() {
    return myDir;
  }

  public GridDirection turnClockwise() {
    return values()[validateIndex(ordinal() + 1)];
  }

  public GridDirection turnCounterclockwise() {
    return values()[validateIndex(ordinal() + 3)];
  }

  public GridDirection opposite() {
    return values()[validateIndex(ordinal() + 2)];
  }

  private int validateIndex(int index) {
    if (index >= values().length) {
      index -= values().length;
    }
    return index;
  }
}