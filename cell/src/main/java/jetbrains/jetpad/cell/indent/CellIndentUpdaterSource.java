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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.indent.updater.IndentUpdaterSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CellIndentUpdaterSource implements IndentUpdaterSource<Cell> {
  private Map<Cell, Registration> myVisibilityRegs = new HashMap<>();
  private Set<Cell> myAttached = new HashSet<>();

  @Override
  public boolean isNewLine(Cell src) {
    return src instanceof NewLineCell;
  }

  @Override
  public boolean isIndented(Cell src) {
    if (src instanceof IndentCell) {
      return ((IndentCell) src).isIndented();
    }

    return false;
  }

  @Override
  public boolean isCell(Cell src) {
    return !(src instanceof IndentCell);
  }

  @Override
  public boolean isVisible(Cell src) {
    return src.visible().get();
  }

  @Override
  public Cell getCell(Cell src) {
    return src;
  }

  @Override
  public boolean isAttached(Cell src) {
    return myAttached.contains(src);
  }

  @Override
  public void setAttached(final Cell src, boolean value) {
    if (value) {
      myAttached.add(src);
    } else {
      myAttached.remove(src);
    }

    if (isCell(src)) {
      if (value) {
        myVisibilityRegs.put(src, src.visible().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            visibilityChanged(src, event);
          }
        }));
      } else {
        myVisibilityRegs.remove(src).remove();
      }
    }
  }

  protected void visibilityChanged(Cell cell, PropertyChangeEvent<Boolean> event) {
  }
}