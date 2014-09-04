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
package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public abstract class CompositeCellTrait extends CellTrait {
  protected abstract CellTrait[] getBaseTraits(Cell cell);

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onPropertyChanged(cell, prop, event);
    }
  }

  @Override
  public void onFocusGained(Cell cell, FocusEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onFocusGained(cell, event);
    }
  }

  @Override
  public void onFocusLost(Cell cell, FocusEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onFocusLost(cell, event);
    }
  }

  @Override
  public void onMousePressed(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMousePressed(cell, event);
    }
  }

  @Override
  public void onMouseReleased(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMouseReleased(cell, event);
    }
  }

  @Override
  public void onMouseMoved(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMouseMoved(cell, event);
    }
  }

  @Override
  public void onMouseDragged(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMouseDragged(cell, event);
    }
  }

  @Override
  public void onMouseEntered(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMouseEntered(cell, event);
    }
  }

  @Override
  public void onMouseLeft(Cell cell, MouseEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onMouseLeft(cell, event);
    }
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyPressed(cell, event);
    }
  }

  @Override
  public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyReleasedLowPriority(cell, event);
    }
  }

  @Override
  public void onKeyReleased(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyReleased(cell, event);
    }
  }

  @Override
  public void onKeyReleasedLowPriority(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyReleasedLowPriority(cell, event);
    }
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyTyped(cell, event);
    }
  }

  @Override
  public void onKeyTypedLowPriority(Cell cell, KeyEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onKeyTypedLowPriority(cell, event);
    }
  }

  @Override
  public void onCopy(Cell cell, CopyCutEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onCopy(cell, event);
    }
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onCut(cell, event);
    }
  }

  @Override
  public void onPaste(Cell cell, PasteEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onPaste(cell, event);
    }
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onComplete(cell, event);
    }
  }

  @Override
  public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.onCellTraitEvent(cell, spec, event);
    }
  }

  @Override
  protected void provideProperties(Cell cell, PropertyCollector collector) {
    for (CellTrait t : getBaseTraits(cell)) {
      t.provideProperties(cell, collector);
    }
  }


  @Override
  public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
    for (CellTrait t : getBaseTraits(cell)) {
      Object result = t.get(cell, spec);
      if (result != null) return result;
    }
    return null;
  }
}
