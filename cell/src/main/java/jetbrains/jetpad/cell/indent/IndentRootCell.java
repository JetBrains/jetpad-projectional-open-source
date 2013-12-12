/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;

public class IndentRootCell extends IndentCell {
  private Listeners<IndentContainerCellListener> myListeners = new Listeners<IndentContainerCellListener>();

  void handleChildAdd(final CollectionItemEvent<Cell> event) {
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.childAdded(event);
      }
    });
  }

  void handleChildRemove(final CollectionItemEvent<Cell> event) {
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.childRemoved(event);
      }
    });
  }

  void handlePropertyChanged(final Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> event) {
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.propertyChanged(cell, prop, event);
      }
    });
  }

  public Registration addListener(IndentContainerCellListener l) {
    return myListeners.add(l);
  }
}