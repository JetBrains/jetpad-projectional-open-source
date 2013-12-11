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
import jetbrains.jetpad.model.children.Composites;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.position.Positions;

import java.util.List;

public class Cells {
  public static boolean isLastChild(Cell cell) {
    Cell parent = cell.parent().get();
    if (parent == null) return false;
    List<Cell> siblings = parent.children();
    int index = siblings.indexOf(cell);
    for (Cell c : siblings.subList(index + 1, siblings.size())) {
      if (c.visible().get()) return false;
    }
    return true;
  }

  public static boolean isFirstChild(Cell cell) {
    Cell parent = cell.parent().get();
    if (parent == null) return false;
    List<Cell> siblings = parent.children();
    int index = siblings.indexOf(cell);

    for (Cell c : siblings.subList(0, index)) {
      if (c.visible().get()) return false;
    }
    return true;
  }

  public static Cell firstFocusable(Cell c) {
    return firstFocusable(c, true);
  }

  public static Cell firstFocusable(Cell c, boolean deepest) {
    for (Cell current : c.children()) {
      if (!current.visible().get()) continue;
      if (!deepest && current.focusable().get()) return current;

      Cell result = firstFocusable(current);
      if (result != null) return result;
    }

    if (c.focusable().get()) return c;

    return null;
  }

  public static Cell lastFocusable(Cell c) {
    return lastFocusable(c, true);
  }

  public static Cell lastFocusable(Cell c, boolean deepest) {
    List<Cell> children = c.children();
    for (int i = children.size() - 1; i >= 0; i--) {
      Cell cc = children.get(i);

      if (!cc.visible().get()) continue;
      if (!deepest && cc.focusable().get()) return cc;

      Cell result = lastFocusable(cc, deepest);
      if (result != null) return result;
    }

    if (c.focusable().get()) return c;
    return null;
  }

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

  public static boolean isDescendant(Cell ancestor, Cell descendant) {
    if (ancestor == descendant) return true;
    if (descendant.parent().get() == null) return false;
    return isDescendant(ancestor, descendant.parent().get());
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

  public static boolean isFocusable(Cell c) {
    if (!c.focusable().get()) return false;

    Cell current = c;
    while (current != null) {
      if (!current.visible().get()) return false;
      current = current.parent().get();
    }

    return true;
  }

  public static boolean isAbove(Cell upper, Cell lower) {
    return upper.origin().y + upper.dimension().y <= lower.origin().y;
  }

  public static  boolean isBelow(Cell lower, Cell upper) {
    return isAbove(upper, lower);
  }

  public static Cell focusableParent(Cell c) {
    Cell parent = c.parent().get();
    if (parent == null) return null;
    if (parent.focusable().get()) return parent;
    return focusableParent(parent);
  }

  public static boolean isVisible(Cell c) {
    if (!c.visible().get()) return false;
    Cell parent = c.parent().get();
    if (parent == null) return true;
    return isVisible(parent);
  }

  public static boolean isEmpty(Cell cell) {
    if (cell instanceof TextCell) {
      return Strings.isNullOrEmpty(((TextCell) cell).text().get());
    }
    return Positions.isFirstPosition(cell) && Positions.isLastPosition(cell);
  }
}