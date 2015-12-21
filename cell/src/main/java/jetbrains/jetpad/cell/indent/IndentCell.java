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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class IndentCell extends Cell {
  private boolean myIndented;
  private Listeners<IndentContainerCellListener> myListeners;

  public IndentCell() {
    this(false);
  }

  public IndentCell(boolean indented) {
    myIndented = indented;
  }

  public boolean isIndented() {
    return myIndented;
  }

  public boolean isRootIndent() {
    if (!isAttached()) return false;
    Cell parent = getParent();
    if (parent instanceof IndentCell) return false;
    return true;
  }

  @Override
  public void scrollTo(Rectangle rect) {
    Cell current = getParent();
    while (current instanceof IndentCell) {
      current = current.getParent();
    }
    current.scrollTo(new Rectangle(rect.origin.add(getBounds().origin.sub(current.getBounds().origin)), rect.dimension));
  }

  public IndentCell indentContainer() {
    if (isRootIndent()) {
      return this;
    }
    Cell parent = getParent();
    if (parent instanceof IndentCell) {
      return ((IndentCell) parent).indentContainer();
    }
    return null;
  }

  @Override
  protected void onPropertySet(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    super.onPropertySet(prop, event);

    IndentCell container = indentContainer();
    if (container == null) return;
    container.handlePropertyChanged(this, prop, event);
  }

  @Override
  protected void onChildAdded(CollectionItemEvent<? extends Cell> event) {
    super.onChildAdded(event);

    IndentCell container = indentContainer();
    if (container == null) return;

    container.handleChildAdd(event);
  }

  @Override
  protected void onBeforeChildRemoved(CollectionItemEvent<Cell> event) {
    super.onBeforeChildRemoved(event);

    IndentCell container = indentContainer();
    if (container == null) return;

    container.handleChildRemove(event);
  }

  void handleChildAdd(final CollectionItemEvent<? extends Cell> event) {
    checkRootIndent();

    if (myListeners == null) return;
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.childAdded(event);
      }
    });
  }

  void handleChildRemove(final CollectionItemEvent<Cell> event) {
    checkRootIndent();

    if (myListeners == null) return;
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.childRemoved(event);
      }
    });
  }

  void handlePropertyChanged(final Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> event) {
    checkRootIndent();

    if (myListeners == null) return;
    myListeners.fire(new ListenerCaller<IndentContainerCellListener>() {
      @Override
      public void call(IndentContainerCellListener l) {
        l.propertyChanged(cell, prop, event);
      }
    });
  }

  private void checkRootIndent() {
    if (!isRootIndent()) {
      throw new IllegalStateException();
    }
  }

  public Registration addListener(IndentContainerCellListener l) {
    checkRootIndent();

    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    final Registration reg = myListeners.add(l);
    return new Registration() {
      @Override
      protected void doRemove() {
        reg.remove();
        if (myListeners.isEmpty()) {
          myListeners = null;
        }
      }
    };
  }
}