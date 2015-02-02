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
package jetbrains.jetpad.cell.util;

import com.google.common.collect.Range;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

import java.util.List;

public class Cells {
  public static final CellTraitEventSpec<Event> BECAME_EMPTY = new CellTraitEventSpec<>("becameEmpty", false);

  public static Cell getNonPopupAncestor(Cell c) {
    Cell current = c;
    while (true) {
      Cell next = getContainingPopup(current);
      if (next == null) return current;
      current = next.getParent();
    }
  }

  private static Cell getContainingPopup(Cell c) {
    Cell current = c;
    while (true) {
      Cell parent = current.getParent();
      if (parent == null) return null;
      if (current.isPopup()) return current;
      current = parent;
    }
  }

  public static boolean isEmpty(Cell cell) {
    PositionHandler handler = cell.get(PositionHandler.PROPERTY);
    return handler.isHome() && handler.isEnd();
  }

  public static boolean isLeaf(Cell cell) {
    return cell instanceof TextCell;
  }

  public static Cell findCell(Cell current, Vector loc) {
    boolean isIndent = current instanceof IndentCell;
    if (!isIndent && !current.getBounds().contains(loc)) return null;
    if (current instanceof NewLineCell) return null;

    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;
      Cell result = findCell(child, loc);
      if (result != null) {
        return result;
      }
    }

    if (!isIndent && current.getBounds().contains(loc)) {
      return current;
    } else {
      return null;
    }
  }

  public static Cell findClosestFocusableToSide(Cell current, Vector loc) {
    if (!current.visible().get()) return null;

    Rectangle bounds = current.getBounds();
    Range<Integer> range = Range.closed(bounds.origin.y, bounds.origin.y + bounds.dimension.y);
    if (!range.contains(loc.y)) {
      return null;
    }
    Cell result = null;
    int distance = Integer.MAX_VALUE;
    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;

      Cell closest = findClosestFocusableToSide(child, loc);
      if (closest == null) continue;
      int newDistance = (int) closest.getBounds().distance(loc);

      if (newDistance < distance) {
        result = closest;
        distance = newDistance;
      }
    }

    if (result == null && current.focusable().get()) {
      return current;
    }

    return result;
  }

  public static Cell firstVisibleLeaf(Cell cell) {
    if (!cell.visible().get()) return null;
    if (cell instanceof NewLineCell) return null;

    for (Cell c : cell.children()) {
      Cell firstVis = firstVisibleLeaf(c);
      if (firstVis != null) return firstVis;
    }

    return cell;
  }

  public static Cell lastVisibleLeaf(Cell cell) {
    if (!cell.visible().get()) return null;
    if (cell instanceof NewLineCell) return null;

    List<Cell> children = cell.children();
    for (int i = children.size() - 1; i >= 0; i--) {
      Cell firstVis = lastVisibleLeaf(children.get(i));
      if (firstVis != null) return firstVis;
    }

    return cell;
  }

  public static Rectangle indentBounds(IndentCell cell) {
    IndentCell container = cell.indentContainer();
    Rectangle bounds = container.getBounds();

    Cell firstLeaf = firstVisibleLeaf(cell);
    if (firstLeaf == null) return null;

    Cell lastLeaf = lastVisibleLeaf(cell);

    Rectangle firstBounds = firstLeaf.getBounds();
    Rectangle lastBounds = lastLeaf.getBounds();

    return new Rectangle(new Vector(bounds.origin.x, firstLeaf.getBounds().origin.y), new Vector(bounds.dimension.x, lastBounds.origin.y + lastBounds.dimension.y - firstBounds.origin.y));
  }
}