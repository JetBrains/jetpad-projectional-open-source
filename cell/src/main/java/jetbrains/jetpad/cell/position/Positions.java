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
package jetbrains.jetpad.cell.position;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.composite.Composites;

public class Positions {
  public static boolean isHomePosition(Cell cell) {
    Cell first = Composites.firstFocusable(cell);
    return first != null && first.focused().get() && first.get(PositionHandler.PROPERTY).isHome();
  }

  public static boolean isEndPosition(Cell cell) {
    Cell last = Composites.lastFocusable(cell);
    return last != null && last.focused().get() && last.get(PositionHandler.PROPERTY).isEnd();
  }

  public static boolean isFirstPosition(Cell cell) {
    Cell firstFocusable = Composites.firstFocusable(cell, true);
    if (firstFocusable == null) return false;
    return firstFocusable.get(PositionHandler.PROPERTY).isHome();
  }

  public static boolean isLastPosition(Cell cell) {
    Cell lastFocusable = Composites.lastFocusable(cell, true);
    if (lastFocusable == null) return false;
    return lastFocusable.focused().get() && lastFocusable.get(PositionHandler.PROPERTY).isEnd();
  }
}