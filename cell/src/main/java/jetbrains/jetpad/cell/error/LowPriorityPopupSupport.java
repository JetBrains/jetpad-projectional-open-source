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
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.HashMap;
import java.util.Map;

class LowPriorityPopupSupport extends Registration implements EventHandler<CollectionItemEvent<? extends Cell>> {
  private Cell myLowPriorityPopup;

  private int myCount = 0;
  private Map<Cell, Registration> myRegistrations = new HashMap<>();
  private EventHandler<PropertyChangeEvent<Cell>> myPopupHandler;
  private EventHandler<PropertyChangeEvent<Boolean>> myPopupVisibleHandler;

  LowPriorityPopupSupport(Cell lowPriorityPopup) {
    myLowPriorityPopup = lowPriorityPopup;

    myPopupVisibleHandler = new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        onChildPopupBecameVisible(event.getNewValue());
      }
    };

    myPopupHandler = new EventHandler<PropertyChangeEvent<Cell>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        onChildPopupChanged(event);
      }
    };

    subscribe(lowPriorityPopup.getParent(), true);
    myLowPriorityPopup.visible().set(myCount == 0);
  }

  private void subscribe(Cell cell, boolean isRoot) {
    myRegistrations.put(cell, doSubscribe(cell, !isRoot));
  }

  private Registration doSubscribe(final Cell cell, boolean checkPopups) {
    CompositeRegistration result = new CompositeRegistration();
    if (checkPopups) {
      for (CellPropertySpec<Cell> spec : Cell.POPUP_SPECS) {
        Property<Cell> prop = cell.getProp(spec);
        if (prop.get() != null) {
          myPopupHandler.onEvent(new PropertyChangeEvent<>(null, prop.get()));
        }
        result.add(prop.addHandler(myPopupHandler));
      }
      result.add(new Registration() {
        @Override
        protected void doRemove() {
          for (CellPropertySpec<Cell> spec : Cell.POPUP_SPECS) {
            Cell popup = cell.get(spec);
            if (popup != null) {
              myPopupHandler.onEvent(new PropertyChangeEvent<>(popup, null));
            }
          }
        }
      });
    }
    result.add(cell.children().addHandler(LowPriorityPopupSupport.this));
    for (Cell child : cell.children()) {
      subscribe(child, false);
    }
    return result;
  }

  @Override
  public void onEvent(CollectionItemEvent<? extends Cell> event) {
    switch (event.getType()) {
      case ADD:
        subscribe(event.getNewItem(), false);
        break;
      case REMOVE:
        myRegistrations.remove(event.getOldItem()).remove();
        break;
      case SET:
        myRegistrations.remove(event.getOldItem()).remove();
        subscribe(event.getNewItem(), false);
    }
  }

  @Override
  protected void doRemove() {
    for (Registration registration : myRegistrations.values()) {
      registration.remove();
    }
    myRegistrations = null;
    myLowPriorityPopup.visible().set(false);
  }

  private void onChildPopupBecameVisible(boolean visible) {
    myCount += (visible ? 1 : -1);
    myLowPriorityPopup.visible().set(myCount == 0);
  }

  private void onChildPopupChanged(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      unsubscribeChildPopup(event.getOldValue());
    }
    if (event.getNewValue() != null) {
      subscribeChildPopup(event.getNewValue());
    }
    myLowPriorityPopup.visible().set(myCount == 0);
  }

  private void subscribeChildPopup(Cell popup) {
    myRegistrations.put(popup, popup.visible().addHandler(myPopupVisibleHandler));
    if (popup.visible().get()) {
      myCount++;
    }
  }

  private void unsubscribeChildPopup(Cell popup) {
    myRegistrations.remove(popup).remove();
    if (popup.visible().get()) {
      myCount--;
    }
  }
}