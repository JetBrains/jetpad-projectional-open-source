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
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.FontFamily;

import java.util.HashMap;
import java.util.Map;

public class ErrorMarkers {
  public static final CellPropertySpec<Cell> ERROR_POPUP_POSITION = Cell.BOTTOM_POPUP;

  static final CellPropertySpec<Boolean> ERROR_POPUP_ACTIVE = new CellPropertySpec<>("isErrorPopupActive", false);

  public static final Color WARNING_COLOR = new Color(244, 232, 171);

  public static Registration install(Cell cell) {
    return new CompositeRegistration(
        cell.addTrait(errorPopupTrait()),
        cell.addTrait(warningTrait(cell)));
  }

  private static CellTrait errorPopupTrait() {
    return new CellTrait() {
      private boolean myEditing = false;
      private boolean myMouseOver = false;
      private Map<Cell, Registration> myRegistrations = null;

      @Override
      public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (prop == Cell.HAS_ERROR) {
          PropertyChangeEvent<Boolean> change = (PropertyChangeEvent<Boolean>) event;
          if (change.getOldValue() && cell.get(ERROR_POPUP_ACTIVE)) {
            removeErrorPopup(cell, cell.get(ERROR_POPUP_POSITION));
          }
          if (change.getNewValue() && cell.get(ERROR_POPUP_POSITION) == null) {
            setErrorPopup(cell);
          }
        } else if (prop == ERROR_POPUP_POSITION && !myEditing) {
          PropertyChangeEvent<Cell> change = (PropertyChangeEvent<Cell>) event;
          if (change.getOldValue() != null && cell.get(ERROR_POPUP_ACTIVE)) {
            removeErrorPopup(cell, change.getOldValue());
          }
          if (change.getNewValue() == null && cell.get(Cell.HAS_ERROR)) {
            setErrorPopup(cell);
          }
        }
      }

      private void setErrorPopup(Cell cell) {
        TextCell popup = new TextCell(" parsing error ");
        popup.visible().set(false);
        popup.set(Cell.HAS_POPUP_DECORATION, true);
        popup.set(Cell.HAS_SHADOW, true);
        popup.set(Cell.BACKGROUND, Color.LIGHT_PINK);
        popup.set(Cell.BORDER_COLOR, Color.GRAY);
        popup.set(TextCell.FONT_FAMILY, FontFamily.SERIF);

        cell.set(ERROR_POPUP_ACTIVE, true);
        updatePopup(cell, popup);
        if (myMouseOver) {
          show(popup);
        }
      }

      private void removeErrorPopup(Cell cell, Cell errorPopup) {
        cell.set(ERROR_POPUP_ACTIVE, false);
        if (cell.get(ERROR_POPUP_POSITION) == errorPopup) {
          updatePopup(cell, null);
        }
        if (myRegistrations != null && myRegistrations.containsKey(errorPopup)) {
          hide(errorPopup);
        }
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
        myMouseOver = true;
        Cell popup = getErrorPopup(cell);
        if (popup != null) {
          show(popup);
        }
      }

      @Override
      public void onMouseLeft(Cell cell, MouseEvent event) {
        myMouseOver = false;
        Cell popup = getErrorPopup(cell);
        if (popup != null) {
          hide(popup);
        }
      }

      private Cell getErrorPopup(Cell cell) {
        return cell.get(ERROR_POPUP_ACTIVE) ? cell.get(ERROR_POPUP_POSITION) : null;
      }

      private void show(Cell popup) {
        if (myRegistrations == null) {
          myRegistrations = new HashMap<>();
        }
        myRegistrations.put(popup, new LowPriorityPopupSupport(popup));
      }

      private void hide(Cell popup) {
        myRegistrations.remove(popup).remove();
        if (myRegistrations.isEmpty()) {
          myRegistrations = null;
        }
      }
    };
  }

  private static CellTrait warningTrait(Cell source) {
    final Boolean hasWarning = source.get(Cell.HAS_WARNING);
    if (hasWarning) {
      source.background().set(WARNING_COLOR);
    }

    return new CellTrait() {
      private boolean myWarning = hasWarning;

      @Override
      public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (prop == Cell.HAS_WARNING) {
          myWarning = ((PropertyChangeEvent<Boolean>) event).getNewValue();
        }
        super.onPropertyChanged(cell, prop, event);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        if (myWarning) {
          collector.add(Cell.BACKGROUND, WARNING_COLOR);
        }
        super.provideProperties(cell, collector);
      }
    };
  }
}
