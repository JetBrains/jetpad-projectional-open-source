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
package jetbrains.jetpad.cell.error;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

public class ErrorMarkers {
  public static final CellPropertySpec<Cell> ERROR_POPUP_POSITION = Cell.BOTTOM_POPUP;

  static final CellPropertySpec<Boolean> ERROR_POPUP_ACTIVE = new CellPropertySpec<>("isErrorPopupActive", false);

  public static Registration install(Cell cell) {
    return cell.addTrait(errorPopupTrait());
  }

  private static CellTrait errorPopupTrait() {
    return new CellTrait() {
      private boolean myEditing = false;

      @Override
      public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (prop == Cell.HAS_ERROR) {
          if (((PropertyChangeEvent<Boolean>) event).getNewValue()) {
            if (cell.get(ERROR_POPUP_POSITION) == null) {
              setErrorPopup(cell);
            }
          } else {
            if (cell.get(ERROR_POPUP_POSITION) != null && cell.get(ERROR_POPUP_ACTIVE)) {
              cell.set(ERROR_POPUP_ACTIVE, false);
              updatePopup(cell, null);
            }
          }
        } else if (prop == ERROR_POPUP_POSITION && !myEditing) {
          if (((PropertyChangeEvent<Cell>) event).getNewValue() == null) {
            if (cell.get(Cell.HAS_ERROR)) {
              setErrorPopup(cell);
            }
          } else {
            cell.set(ERROR_POPUP_ACTIVE, false);
          }
        }
      }

      private void setErrorPopup(Cell cell) {
        TextCell popup = new TextCell("parsing error");
        popup.visible().set(false);
        popup.set(Cell.HAS_POPUP_DECORATION, true);
        popup.set(Cell.HAS_SHADOW, true);
        popup.set(Cell.BACKGROUND, Color.LIGHT_PINK);
        popup.set(Cell.BORDER_COLOR, Color.GRAY);
        cell.set(ERROR_POPUP_ACTIVE, true);
        updatePopup(cell, popup);
      }

      private void updatePopup(Cell cell, Cell popup) {
        myEditing = true;
        try {
          cell.set(ERROR_POPUP_POSITION, popup);
        } finally {
          myEditing = false;
        }
      }

      @Override
      public void onMouseEntered(Cell cell, MouseEvent event) {
        Cell popup = getErrorPopup(cell);
        if (popup != null) {
          popup.visible().set(true);
        }
      }

      @Override
      public void onMouseLeft(Cell cell, MouseEvent event) {
        Cell popup = getErrorPopup(cell);
        if (popup != null) {
          popup.visible().set(false);
        }
      }

      private Cell getErrorPopup(Cell cell) {
        return cell.get(ERROR_POPUP_ACTIVE) ? cell.get(ERROR_POPUP_POSITION) : null;
      }
    };
  }
}
