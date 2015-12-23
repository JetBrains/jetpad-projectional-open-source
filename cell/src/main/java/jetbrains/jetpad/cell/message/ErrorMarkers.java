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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.FontFamily;

import java.util.HashMap;
import java.util.Map;

public class ErrorMarkers {
  static final CellPropertySpec<Cell> POPUP_POSITION = Cell.BOTTOM_POPUP;
  static final CellPropertySpec<Boolean> POPUP_ACTIVE = new CellPropertySpec<>("isMessagePopupActive", false);

  public static Registration install(Cell cell) {
    return cell.addTrait(popupTrait());
  }

  private static CellTrait popupTrait() {
    return new CellTrait() {
      private boolean myEditingPopup = false;
      private Map<Cell, Registration> myRegistrations = null;

      @Override
      public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (prop == Cell.HAS_ERROR) {
          PropertyChangeEvent<Boolean> change = (PropertyChangeEvent<Boolean>) event;
          if (change.getOldValue() && cell.get(POPUP_ACTIVE)) {
            removePopup(cell, cell.get(POPUP_POSITION));
          }
          if (change.getNewValue() && cell.get(POPUP_POSITION) == null) {
            setPopup(cell);
          }
        } else if (prop == POPUP_POSITION && !myEditingPopup) {
          PropertyChangeEvent<Cell> change = (PropertyChangeEvent<Cell>) event;
          if (change.getOldValue() != null && cell.get(POPUP_ACTIVE)) {
            removePopup(cell, change.getOldValue());
          }
          if (change.getNewValue() == null && cell.get(Cell.HAS_ERROR)) {
            setPopup(cell);
          }
        }
        super.onPropertyChanged(cell, prop, event);
      }

      private void setPopup(Cell cell) {
        TextCell popup = new TextCell();
        popup.visible().set(false);
        popup.set(Cell.HAS_POPUP_DECORATION, true);
        popup.set(Cell.HAS_SHADOW, true);
        popup.set(Cell.BACKGROUND, Color.VERY_LIGHT_YELLOW);
        popup.set(Cell.BORDER_COLOR, Color.GRAY);
        popup.set(TextCell.FONT_FAMILY, FontFamily.SERIF);
        popup.set(TextCell.TEXT, " parsing error ");

        cell.set(POPUP_ACTIVE, true);
        updatePopupValue(cell, popup);
      }

      private void removePopup(Cell cell, Cell popup) {
        cell.set(POPUP_ACTIVE, false);
        if (cell.get(POPUP_POSITION) == popup) {
          updatePopupValue(cell, null);
        }
        if (myRegistrations != null && myRegistrations.containsKey(popup)) {
          hide(popup);
        }
      }

      private void updatePopupValue(Cell cell, Cell newPopup) {
        myEditingPopup = true;
        try {
          cell.set(POPUP_POSITION, newPopup);
        } finally {
          myEditingPopup = false;
        }
      }

      @Override
      public void onMouseEntered(Cell cell, MouseEvent event) {
        Cell popup = getMessagePopup(cell);
        if (popup != null) {
          show(popup);
          event.consume();
        }
      }

      @Override
      public void onMouseLeft(Cell cell, MouseEvent event) {
        Cell popup = getMessagePopup(cell);
        if (popup != null) {
          hide(popup);
          event.consume();
        }
      }

      @Override
      public void onMouseMoved(Cell cell, MouseEvent event) {
        Cell popup = getMessagePopup(cell);
        if (popup != null && (myRegistrations == null || !myRegistrations.containsKey(popup))) {
          show(popup);
          event.consume();
        }
      }

      private Cell getMessagePopup(Cell cell) {
        return cell.get(POPUP_ACTIVE) ? cell.get(POPUP_POSITION) : null;
      }

      private void show(Cell popup) {
        if (myRegistrations == null) {
          myRegistrations = new HashMap<>();
        }
        if (!myRegistrations.containsKey(popup)) {
          myRegistrations.put(popup, new LowPriorityPopupSupport(popup));
        }
      }

      private void hide(Cell popup) {
        if (myRegistrations == null) return;
        Registration registration = myRegistrations.remove(popup);
        if (registration != null) {
          registration.remove();
        }
        if (myRegistrations.isEmpty()) {
          myRegistrations = null;
        }
      }
    };
  }
}
