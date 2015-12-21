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
import jetbrains.jetpad.values.FontFamily;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ErrorDecorationTrait extends CellTrait {
  static final CellPropertySpec<Cell> POPUP_POSITION = Cell.BOTTOM_POPUP;
  static final CellPropertySpec<Boolean> POPUP_ACTIVE = new CellPropertySpec<>("isErrorPopupActive", false);

  private static final List<CellPropertySpec<String>> ERROR_PROPS_PRIORITY =
      Arrays.asList(ErrorController.BROKEN, ErrorController.ERROR, ErrorController.WARNING);

  private static Color popupBackground(CellPropertySpec<String> prop) {
    return prop == ErrorController.ERROR ? Color.LIGHT_PINK : Color.VERY_LIGHT_YELLOW;
  }

  private boolean myEditingPopup = false;
  private Map<Cell, Registration> myRegistrations = null;
  private ErrorStyleController myErrorStyler;

  ErrorDecorationTrait(ErrorStyleController styleController) {
    myErrorStyler = styleController;
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    if (ERROR_PROPS_PRIORITY.contains(prop)) {
      updateDecorations(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);
      updatePopup(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);

    } else if (prop == POPUP_POSITION && !myEditingPopup) {
      PropertyChangeEvent<Cell> change = (PropertyChangeEvent<Cell>) event;
      if (change.getOldValue() != null && cell.get(POPUP_ACTIVE)) {
        removeErrorPopup(cell, change.getOldValue());
      }
      if (change.getNewValue() == null) {
        CellPropertySpec<String> error = getFirstNotNullProp(cell);
        if (error != null) {
          setPopup(cell, cell.get(error), popupBackground(error));
        }
      }
    }

    super.onPropertyChanged(cell, prop, event);
  }

  private void updateDecorations(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> change) {
    if (change.getOldValue() != null && change.getNewValue() != null) return;
    boolean apply = change.getOldValue() == null;
    if (prop == ErrorController.BROKEN) {
      myErrorStyler.applyBroken(cell, apply);
    } else if (prop == ErrorController.ERROR) {
      myErrorStyler.applyError(cell, apply);
    } else if (prop == ErrorController.WARNING) {
      myErrorStyler.applyWarning(cell, apply);
    }
  }

  private CellPropertySpec<String> getFirstNotNullProp(Cell cell) {
    if (cell.get(ErrorController.BROKEN) != null) return ErrorController.BROKEN;
    if (cell.get(ErrorController.ERROR) != null) return ErrorController.ERROR;
    return cell.get(ErrorController.WARNING) == null ? null : ErrorController.WARNING;
  }

  private void updatePopup(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> change) {
    if (change.getNewValue() == null && cell.get(POPUP_ACTIVE)) {
      TextCell popup = (TextCell) cell.get(POPUP_POSITION);
      CellPropertySpec<String> next = getNextNotNullProp(cell, prop);
      if (next == null) {
        removeErrorPopup(cell, popup);
      } else {
        update(popup, cell.get(next), popupBackground(next));
      }
      return;
    }

    if (change.getOldValue() == null) {
      if (cell.get(POPUP_ACTIVE)) {
        TextCell popup = (TextCell) cell.get(POPUP_POSITION);
        CellPropertySpec<String> withHighestPriority = getFirstNotNullProp(cell);
        if (priority(prop, withHighestPriority)) {
          update(popup, change.getNewValue(), popupBackground(prop));
        }
      } else if (cell.get(POPUP_POSITION) == null) {
        setPopup(cell, cell.get(prop), popupBackground(prop));
      }
      return;
    }

    if (cell.get(POPUP_ACTIVE)) {
      TextCell popup = (TextCell) cell.get(POPUP_POSITION);
      CellPropertySpec<String> withHighestPriority = getFirstNotNullProp(cell);
      if (prop == withHighestPriority) {
        update(popup, change.getNewValue(), popupBackground(prop));
      }
    }
  }

  private boolean priority(CellPropertySpec<String> p1, CellPropertySpec<String> p2) {
    return ERROR_PROPS_PRIORITY.indexOf(p1) <= ERROR_PROPS_PRIORITY.indexOf(p2);
  }

  private void update(TextCell popup, String message, Color background) {
    popup.text().set(" " + message + " ");
    popup.set(Cell.BACKGROUND, background);
  }

  private CellPropertySpec<String> getNextNotNullProp(Cell cell, CellPropertySpec<String> prop) {
    if (prop == ErrorController.ERROR) {
      if (cell.get(ErrorController.BROKEN) != null) return ErrorController.BROKEN;
      return cell.get(ErrorController.WARNING) == null ? null : ErrorController.WARNING;
    }
    if (prop == ErrorController.BROKEN) {
      if (cell.get(ErrorController.ERROR) != null) return ErrorController.ERROR;
      return cell.get(ErrorController.WARNING) == null ? null : ErrorController.WARNING;
    }
    if (prop == ErrorController.WARNING) {
      if (cell.get(ErrorController.ERROR) != null) return ErrorController.ERROR;
      return cell.get(ErrorController.BROKEN) == null ? null : ErrorController.BROKEN;
    }
    throw new IllegalStateException();
  }

  private void setPopup(Cell cell, String message, Color background) {
    TextCell popup = new TextCell();
    popup.visible().set(false);
    popup.set(Cell.HAS_POPUP_DECORATION, true);
    popup.set(Cell.HAS_SHADOW, true);
    popup.set(Cell.BACKGROUND, background);
    popup.set(Cell.BORDER_COLOR, Color.GRAY);
    popup.set(TextCell.FONT_FAMILY, FontFamily.SERIF);
    update(popup, message, background);

    cell.set(POPUP_ACTIVE, true);
    updateCellPopup(cell, popup);
  }

  private void removeErrorPopup(Cell cell, Cell errorPopup) {
    cell.set(POPUP_ACTIVE, false);
    if (cell.get(POPUP_POSITION) == errorPopup) {
      updateCellPopup(cell, null);
    }
    if (myRegistrations != null && myRegistrations.containsKey(errorPopup)) {
      hide(errorPopup);
    }
  }

  private void updateCellPopup(Cell cell, Cell popup) {
    myEditingPopup = true;
    try {
      cell.set(POPUP_POSITION, popup);
    } finally {
      myEditingPopup = false;
    }
  }

  @Override
  public void onMouseEntered(Cell cell, MouseEvent event) {
    Cell popup = getErrorPopup(cell);
    if (popup != null) {
      show(popup);
      event.consume();
    }
  }

  @Override
  public void onMouseLeft(Cell cell, MouseEvent event) {
    Cell popup = getErrorPopup(cell);
    if (popup != null) {
      hide(popup);
      event.consume();
    }
  }

  @Override
  public void onMouseMoved(Cell cell, MouseEvent event) {
    Cell popup = getErrorPopup(cell);
    if (popup != null && (myRegistrations == null || !myRegistrations.containsKey(popup))) {
      show(popup);
      event.consume();
    }
  }

  private Cell getErrorPopup(Cell cell) {
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

  void detach(Cell cell) {
    myErrorStyler.detach(cell);

    if (myRegistrations == null) return;
    Registration registration = myRegistrations.remove(cell);
    if (registration == null) return;
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
    registration.remove();
  }
}