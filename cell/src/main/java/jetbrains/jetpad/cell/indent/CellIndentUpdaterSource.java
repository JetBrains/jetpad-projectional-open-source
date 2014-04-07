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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.indent.updater.IndentUpdaterSource;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.HashSet;
import java.util.Set;

public class CellIndentUpdaterSource implements IndentUpdaterSource<Cell> {
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
  }

  @Override
  public Registration watch(final Cell child) {
    if (isCell(child)) {
      return child.visible().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Boolean> event) {
          visibilityChanged(child, event);
        }
      });
    } else {
      return Registration.EMPTY;
    }
  }

  protected void visibilityChanged(Cell cell, PropertyChangeEvent<Boolean> event) {
  }
}