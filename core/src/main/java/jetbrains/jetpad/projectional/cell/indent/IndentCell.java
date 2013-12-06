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
package jetbrains.jetpad.projectional.cell.indent;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.CellPropertySpec;

public class IndentCell extends Cell {
  private boolean myIndented;

  public IndentCell() {
    this(false);
  }

  public IndentCell(boolean indented) {
    myIndented = indented;
  }

  public boolean isIndented() {
    return myIndented;
  }

  private IndentRootCell indentContainer() {
    if (this instanceof IndentRootCell) {
      return (IndentRootCell) this;
    }
    Cell parent = parent().get();
    if (parent instanceof IndentCell) {
      return ((IndentCell) parent).indentContainer();
    }
    return null;
  }

  @Override
  protected void onPropertySet(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    super.onPropertySet(prop, event);

    IndentRootCell container = indentContainer();
    if (container == null) return;
    container.handlePropertyChanged(this, prop, event);
  }

  @Override
  protected void onChildAdded(CollectionItemEvent<Cell> event) {
    super.onChildAdded(event);

    IndentRootCell container = indentContainer();
    if (container == null) return;

    container.handleChildAdd(event);
  }

  @Override
  protected void onBeforeChildRemoved(CollectionItemEvent<Cell> event) {
    super.onBeforeChildRemoved(event);

    IndentRootCell container = indentContainer();
    if (container == null) return;

    container.handleChildRemove(event);
  }
}