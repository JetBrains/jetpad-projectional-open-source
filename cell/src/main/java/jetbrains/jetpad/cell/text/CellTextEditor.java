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
package jetbrains.jetpad.cell.text;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.ReadableProperty;

abstract class CellTextEditor implements TextEditor {
  private Cell myCell;
  private CompositeRegistration myCompletionHideRegistrations = null;

  CellTextEditor(Cell cell) {
    myCell = cell;
  }

  Cell getCell() {
    return myCell;
  }

  @Override
  public boolean isFirstAllowed() {
    return myCell.get(TextEditing.FIRST_ALLOWED);
  }

  @Override
  public boolean isLastAllowed() {
    return myCell.get(TextEditing.LAST_ALLOWED);
  }

  @Override
  public Vector dimension() {
    return myCell.dimension();
  }

  @Override
  public void focus() {
    myCell.focus();
  }

  @Override
  public ReadableProperty<Boolean> focused() {
    return myCell.focused();
  }

  @Override
  public ReadableProperty<Boolean> focusable() {
    return myCell.focusable();
  }

  @Override
  public ReadableProperty<Boolean> focusHighlighted() {
    return myCell.focusHighlighted();
  }

  @Override
  public void setCompletionItems(Object items) {
    myCell.bottomPopup().set((Cell) items);
  }

  @Override
  public void addHideCompletionRegistration(Registration onHide) {
    if (myCompletionHideRegistrations == null) {
      myCompletionHideRegistrations = new CompositeRegistration();
    }
    myCompletionHideRegistrations.add(onHide);
  }

  @Override
  public void onCompletionHidden() {
    if (myCompletionHideRegistrations != null) {
      Registration r = myCompletionHideRegistrations;
      myCompletionHideRegistrations = null;
      r.remove();
    }
  }

  @Override
  public Registration addKeyPressedHandler(final EventHandler<KeyEvent> handler) {
    return myCell.addTrait(new CellTrait() {
      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        handler.onEvent(event);
      }
    });
  }

  @Override
  public Registration addKeyPressedLowPriorityHandler(final EventHandler<KeyEvent> handler) {
    return myCell.addTrait(new CellTrait() {
      @Override
      public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
        handler.onEvent(event);
      }
    });
  }

  @Override
  public String toString() {
    return "TextEditor(" + myCell + ')';
  }
}
