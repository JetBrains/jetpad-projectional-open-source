/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import jetbrains.jetpad.model.children.Composites;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.position.Positions;

import java.util.List;

import static jetbrains.jetpad.model.children.Composites.firstFocusable;
import static jetbrains.jetpad.model.children.Composites.isFocusable;

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

  public static Cell firstFocusableLeaf(Cell c) {
    if (c.children().isEmpty()) {
      if (c.focusable().get()) return c;
      return null;
    } else {
      return firstFocusable(c.children().get(0));
    }
  }

  public static Cell lastFocusableLeaf(Cell c) {
    if (c.children().isEmpty()) {
      if (c.focusable().get()) return c;
      return null;
    } else {
      return firstFocusable(c.children().get(c.children().size() - 1));
    }
  }

  public static Cell nextFocusable(Cell cell) {
    for (Cell c : Composites.nextLeaves(cell)) {
      if (isFocusable(c)) return c;
    }
    return null;
  }

  public static Cell prevFocusable(Cell cell) {
    for (Cell c : Composites.prevLeaves(cell)) {
      if (isFocusable(c)) return c;
    }
    return null;
  }

  public static Cell homeElement(Cell cell) {
    Cell current = cell;
    while (true) {
      Cell prev = prevFocusable(current);
      if (prev == null || isAbove(prev, cell)) return current;
      current = prev;
    }
  }

  public static Cell endElement(Cell cell) {
    Cell current = cell;
    while (true) {
      Cell next = nextFocusable(current);
      if (next == null || isBelow(next, cell)) return current;
      current = next;
    }
  }

  public static boolean isAbove(Cell upper, Cell lower) {
    return upper.origin().y + upper.dimension().y <= lower.origin().y;
  }

  public static  boolean isBelow(Cell lower, Cell upper) {
    return isAbove(upper, lower);
  }

  public static boolean isEmpty(Cell cell) {
    if (cell instanceof TextCell) {
      return Strings.isNullOrEmpty(((TextCell) cell).text().get());
    }
    return Positions.isFirstPosition(cell) && Positions.isLastPosition(cell);
  }
}