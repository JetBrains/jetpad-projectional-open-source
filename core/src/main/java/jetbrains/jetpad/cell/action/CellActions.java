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
package jetbrains.jetpad.cell.action;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.position.PositionHandler;

public class CellActions {
  public static CellAction seq(final CellAction... actions) {
    return new CellAction() {
      @Override
      public void execute() {
        for (CellAction a : actions) {
          a.execute();
        }
      }
    };
  }

  public static CellAction toCell(final Cell cell) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        if (!cell.focusable().get()) {
          throw new IllegalStateException();
        }
        cell.focus();
      }
    };
  }

  public static CellAction toHome(final Cell cell) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        if (!cell.focusable().get()) {
          throw new IllegalStateException();
        }
        cell.get(PositionHandler.PROPERTY).home();
        cell.focus();
      }
    };
  }

  public static CellAction toEnd(final Cell cell) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        if (!cell.focusable().get()) {
          throw new IllegalStateException();
        }
        cell.get(PositionHandler.PROPERTY).end();
        cell.focus();
      }
    };
  }

  public static CellAction toFirstFocusable(final Cell cell) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        toHome(Cells.firstFocusable(cell)).execute();
      }
    };
  }

  public static CellAction toLastFocusable(final Cell cell) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        Cell lf = Cells.lastFocusable(cell);
        if (lf == null) {
          Cells.lastFocusable(cell);
        }
        toEnd(lf).execute();
      }
    };
  }

  public static CellAction toPosition(final Cell cell, final int pos) {
    if (cell instanceof TextCell) {
      return toPosition((TextCell) cell, pos);
    } else {
      return toCell(cell);
    }
  }

  public static CellAction toPosition(final TextCell cell, final int pos) {
    if (cell == null) throw new NullPointerException();

    return new CellAction() {
      @Override
      public void execute() {
        cell.focus();
        cell.caretPosition().set(pos);
      }
    };
  }
}