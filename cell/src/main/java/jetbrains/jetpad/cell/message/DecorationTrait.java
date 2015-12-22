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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DecorationTrait extends CellTrait {
  static final CellPropertySpec<Cell> POPUP_POSITION = Cell.BOTTOM_POPUP;
  static final CellPropertySpec<Boolean> POPUP_ACTIVE = new CellPropertySpec<>("isMessagePopupActive", false);

  private static final List<CellPropertySpec<String>> ERROR_PROPS_PRIORITY =
      Arrays.asList(MessageController.BROKEN, MessageController.ERROR, MessageController.WARNING);

  private boolean myEditingPopup = false;
  private Map<Cell, Registration> myRegistrations = null;
  private StyleController myStyler;

  DecorationTrait(StyleController styleController) {
    myStyler = styleController;
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    if (ERROR_PROPS_PRIORITY.contains(prop)) {
      updateDecorations(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);
      updatePopup(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);

    } else if (prop == POPUP_POSITION && !myEditingPopup) {
      PropertyChangeEvent<Cell> change = (PropertyChangeEvent<Cell>) event;
      if (change.getOldValue() != null && cell.get(POPUP_ACTIVE)) {
        removePopup(cell, change.getOldValue());
      }
      if (change.getNewValue() == null) {
        CellPropertySpec<String> message = getFirstNotNullProp(cell);
        if (message != null) {
          setPopup(cell, cell.get(message));
        }
      }
    }

    super.onPropertyChanged(cell, prop, event);
  }

  private void setPopup(Cell cell, String message) {
    TextCell popup = new TextCell();
    popup.visible().set(false);
    popup.set(Cell.HAS_POPUP_DECORATION, true);
    popup.set(Cell.HAS_SHADOW, true);
    popup.set(Cell.BACKGROUND, Color.VERY_LIGHT_YELLOW);
    popup.set(Cell.BORDER_COLOR, Color.GRAY);
    popup.set(TextCell.FONT_FAMILY, FontFamily.SERIF);
    updateMessage(popup, message);

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

  private void updateDecorations(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> change) {
    if (change.getOldValue() != null && change.getNewValue() != null) return;
    boolean apply = change.getOldValue() == null;
    if (prop == MessageController.BROKEN) {
      myStyler.applyBroken(cell, apply);
    } else if (prop == MessageController.ERROR) {
      myStyler.applyError(cell, apply);
    } else if (prop == MessageController.WARNING) {
      myStyler.applyWarning(cell, apply);
    }
  }

  private CellPropertySpec<String> getFirstNotNullProp(Cell cell) {
    if (cell.get(MessageController.BROKEN) != null) return MessageController.BROKEN;
    if (cell.get(MessageController.ERROR) != null) return MessageController.ERROR;
    return cell.get(MessageController.WARNING) == null ? null : MessageController.WARNING;
  }

  private void updatePopup(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> change) {
    if (change.getNewValue() == null && cell.get(POPUP_ACTIVE)) {
      TextCell popup = (TextCell) cell.get(POPUP_POSITION);
      CellPropertySpec<String> next = getNextNotNullProp(cell, prop);
      if (next == null) {
        removePopup(cell, popup);
      } else {
        updateMessage(popup, cell.get(next));
      }
      return;
    }

    if (change.getOldValue() == null) {
      if (cell.get(POPUP_ACTIVE)) {
        TextCell popup = (TextCell) cell.get(POPUP_POSITION);
        CellPropertySpec<String> withHighestPriority = getFirstNotNullProp(cell);
        if (priority(prop, withHighestPriority)) {
          updateMessage(popup, change.getNewValue());
        }
      } else if (cell.get(POPUP_POSITION) == null) {
        setPopup(cell, cell.get(prop));
      }
      return;
    }

    if (cell.get(POPUP_ACTIVE)) {
      TextCell popup = (TextCell) cell.get(POPUP_POSITION);
      CellPropertySpec<String> withHighestPriority = getFirstNotNullProp(cell);
      if (prop == withHighestPriority) {
        updateMessage(popup, change.getNewValue());
      }
    }
  }

  private boolean priority(CellPropertySpec<String> p1, CellPropertySpec<String> p2) {
    return ERROR_PROPS_PRIORITY.indexOf(p1) <= ERROR_PROPS_PRIORITY.indexOf(p2);
  }

  private void updateMessage(TextCell popup, String message) {
    popup.text().set(" " + message + " ");
  }

  private CellPropertySpec<String> getNextNotNullProp(Cell cell, CellPropertySpec<String> prop) {
    if (prop == MessageController.ERROR) {
      if (cell.get(MessageController.BROKEN) != null) return MessageController.BROKEN;
      return cell.get(MessageController.WARNING) == null ? null : MessageController.WARNING;
    }
    if (prop == MessageController.BROKEN) {
      if (cell.get(MessageController.ERROR) != null) return MessageController.ERROR;
      return cell.get(MessageController.WARNING) == null ? null : MessageController.WARNING;
    }
    if (prop == MessageController.WARNING) {
      if (cell.get(MessageController.ERROR) != null) return MessageController.ERROR;
      return cell.get(MessageController.BROKEN) == null ? null : MessageController.BROKEN;
    }
    throw new IllegalStateException();
  }

  void detach(Cell cell) {
    myStyler.detach(cell);

    if (myRegistrations == null) return;
    Registration registration = myRegistrations.remove(cell);
    if (registration == null) return;
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
    registration.remove();
  }
}