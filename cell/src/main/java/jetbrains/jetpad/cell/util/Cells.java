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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.geometry.Vector;

import java.util.List;

public class Cells {
  public static final CellTraitEventSpec<Event> BECAME_EMPTY = new CellTraitEventSpec<>("becameEmpty", false);

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
}