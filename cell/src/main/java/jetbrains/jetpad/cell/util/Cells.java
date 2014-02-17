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
package jetbrains.jetpad.cell.util;

import com.google.common.base.Strings;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.position.Positions;

import java.util.List;

import static jetbrains.jetpad.model.composite.Composites.firstFocusable;

public class Cells {
  public static final CellTraitEventSpec<Event> BECAME_EMPTY = new CellTraitEventSpec<Event>("becameEmpty", false);

  public static Cell getNonPopupAncestor(Cell c) {
    Cell current = c;
    while (true) {
      Cell next = getContainingPopup(current);
      if (next == null) return current;
      current = next.parent().get();
    }
  }

  private static Cell getContainingPopup(Cell c) {
    Cell current = c;
    while (true) {
      Cell parent = current.parent().get();
      if (parent == null) return null;
      List<Cell> siblings = parent.children();
      if (siblings.indexOf(current) == -1) return current;
      current = parent;
    }
  }

  public static boolean isEmpty(Cell cell) {
    if (cell instanceof TextCell) {
      return Strings.isNullOrEmpty(((TextCell) cell).text().get());
    }
    return Positions.isFirstPosition(cell) && Positions.isLastPosition(cell);
  }
}