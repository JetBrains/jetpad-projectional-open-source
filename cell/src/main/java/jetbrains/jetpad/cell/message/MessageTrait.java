/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import jetbrains.jetpad.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.FontFamily;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MessageTrait extends CellTrait {
  static final CellPropertySpec<Cell> POPUP_POSITION = Cell.TOP_POPUP;
  static final CellPropertySpec<Boolean> POPUP_ACTIVE = new CellPropertySpec<>("isMessagePopupActive", false);

  static final int POPUPS_SHOW_DELAY_MILLIS = 300;

  private static final List<CellPropertySpec<String>> MESSAGES_IN_PRIORITY_ORDER =
      Arrays.asList(MessageController.INFO, MessageController.BROKEN, MessageController.ERROR, MessageController.WARNING);

  private Registration myUpdatesReg = null;
  private long myLastEditingKeyEvent = 0;
  private boolean myForceHide = false;
  private final Map<Cell, Map<CellPropertySpec<String>, PropertyChangeEvent<String>>> myPendingUpdates = new HashMap<>();

  // this field is required only as EDT supplier, but the appropriate EDT may be set after MessageController is installed
  private CellContainer myContainer;

  private boolean myEditingPopup = false;
  private Map<Cell, LowPriorityPopupSupport> myRegistrations = null;
  private StyleApplicator myStyler;

  MessageTrait(CellContainer container, StyleApplicator styleApplicator) {
    myContainer = container;
    myStyler = styleApplicator;
  }

  @Override
  public void onAdd(Cell cell) {
    for (CellPropertySpec<String> prop : MESSAGES_IN_PRIORITY_ORDER) {
      String value = cell.get(prop);
      if (value != null) {
        onPropertyChanged(cell, prop, new PropertyChangeEvent<>(null, value));
      }
    }
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    if (MESSAGES_IN_PRIORITY_ORDER.contains(prop)) {
      if (myForceHide) {
        postpone(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);
      } else {
        apply(cell, (CellPropertySpec<String>) prop, (PropertyChangeEvent<String>) event);
      }
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

  private void postpone(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> event) {
    if (!myPendingUpdates.containsKey(cell)) {
      myPendingUpdates.put(cell, new HashMap<CellPropertySpec<String>, PropertyChangeEvent<String>>());
    }
    Map<CellPropertySpec<String>, PropertyChangeEvent<String>> updates = myPendingUpdates.get(cell);
    if (updates.containsKey(prop)) {
      String oldValue = updates.get(prop).getOldValue();
      if (Objects.equal(oldValue,  event.getNewValue())) {
        updates.remove(prop);
      } else {
        updates.put(prop, new PropertyChangeEvent<>(oldValue, event.getNewValue()));
      }
    } else {
      updates.put(prop, event);
    }
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
    hide(popup);
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
  public void onKeyTyped(Cell cell, KeyEvent event) {
    onEditingKeyEvent();
  }

  private void onEditingKeyEvent() {
    myLastEditingKeyEvent = myContainer.getEdt().getCurrentTimeMillis();
    if (myForceHide) return;
    setForceHide(true);
    myUpdatesReg = myContainer.getEdt().scheduleRepeating(POPUPS_SHOW_DELAY_MILLIS, new Runnable() {
      @Override
      public void run() {
        if (myContainer.getEdt().getCurrentTimeMillis() - myLastEditingKeyEvent < POPUPS_SHOW_DELAY_MILLIS) return;
        myUpdatesReg.remove();
        myUpdatesReg = null;
        setForceHide(false);
        for (Map.Entry<Cell, Map<CellPropertySpec<String>, PropertyChangeEvent<String>>> cellEntry : myPendingUpdates.entrySet()) {
          Cell cell = cellEntry.getKey();
          for (Map.Entry<CellPropertySpec<String>, PropertyChangeEvent<String>> propEntry : cellEntry.getValue().entrySet()) {
            apply(cell, propEntry.getKey(), propEntry.getValue());
          }
        }
        myPendingUpdates.clear();
      }
    });
  }

  private void setForceHide(boolean forceHide) {
    if (myForceHide == forceHide) return;
    myForceHide = forceHide;
    if (myRegistrations == null) return;
    for (LowPriorityPopupSupport support : myRegistrations.values()) {
      support.setForceHide(myForceHide);
    }
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    if (cell instanceof TextCell && (event.is(Key.BACKSPACE) || event.is(Key.DELETE) || event.is(KeyStrokeSpecs.DELETE_CURRENT))) {
      onEditingKeyEvent();
      return;
    }
    if (cell.get(Cell.FOCUSED) || !cell.get(Cell.FOCUSABLE)) {
      if (event.is(KeyStrokeSpecs.HELP)) {
        if (showPopup(cell)) {
          event.consume();
        }
        return;
      } else if (event.is(Key.ESCAPE)) {
        hidePopup(cell);
        return;
      }
    }
    super.onKeyPressed(cell, event);
  }

  @Override
  public void onFocusLost(Cell cell, FocusEvent event) {
    hidePopup(cell);
  }

  @Override
  public void onMouseEntered(Cell cell, MouseEvent event) {
    if (showPopup(cell)) {
      event.consume();
    }
  }

  @Override
  public void onMouseLeft(Cell cell, MouseEvent event) {
    hidePopup(cell);
  }

  @Override
  public void onMouseMoved(Cell cell, MouseEvent event) {
    if (showPopup(cell)) {
      event.consume();
    }
  }

  private boolean showPopup(Cell cell) {
    Cell popup = getMessagePopup(cell);
    return popup != null && show(popup);
  }

  private boolean hidePopup(Cell cell) {
    Cell popup = getMessagePopup(cell);
    return popup != null && hide(popup);
  }

  private Cell getMessagePopup(Cell cell) {
    return cell.get(POPUP_ACTIVE) ? cell.get(POPUP_POSITION) : null;
  }

  private boolean show(Cell popup) {
    if (myRegistrations == null) {
      myRegistrations = new HashMap<>();
    } else if (myRegistrations.containsKey(popup)) {
      return false;
    }
    myRegistrations.put(popup, new LowPriorityPopupSupport(popup, myForceHide));
    return true;
  }

  private boolean hide(Cell popup) {
    if (myRegistrations == null) return false;
    Registration registration = myRegistrations.remove(popup);
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
    if (registration != null) {
      registration.remove();
      return true;
    }
    return false;
  }

  private void apply(Cell cell, CellPropertySpec<String> prop, PropertyChangeEvent<String> change) {
    updateDecorations(cell, prop, change);
    updatePopup(cell, prop, change);
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
    } else if (prop == MessageController.INFO) {
      myStyler.applyInfo(cell, apply);
    } else {
      throw new IllegalStateException("Unexpected property spec: " + prop);
    }
  }

  private CellPropertySpec<String> getFirstNotNullProp(Cell cell) {
    for (CellPropertySpec<String> p : MESSAGES_IN_PRIORITY_ORDER) {
      if (cell.get(p) != null) return p;
    }
    return null;
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
    return MESSAGES_IN_PRIORITY_ORDER.indexOf(p1) <= MESSAGES_IN_PRIORITY_ORDER.indexOf(p2);
  }

  private void updateMessage(TextCell popup, String message) {
    popup.text().set(" " + message + " ");
  }

  private CellPropertySpec<String> getNextNotNullProp(Cell cell, CellPropertySpec<String> prop) {
    for (CellPropertySpec<String> p : MESSAGES_IN_PRIORITY_ORDER) {
      if (p != prop && cell.get(p) != null) return p;
    }
    return null;
  }

  void detach(Cell cell) {
    myStyler.detach(cell);
    myPendingUpdates.remove(cell);
    if (myRegistrations == null) return;
    Registration registration = myRegistrations.remove(cell);
    if (registration == null) return;
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
    registration.remove();
  }
}