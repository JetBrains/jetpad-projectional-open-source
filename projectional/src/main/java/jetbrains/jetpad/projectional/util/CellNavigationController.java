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
package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.Selector;
import jetbrains.jetpad.projectional.generic.NavigationController;

//todo move it out of cell module
public class CellNavigationController extends NavigationController<Cell> {
  static Registration install(final CellContainer container) {
    final CellNavigationController controller = new CellNavigationController(container);
    return controller.install();
  }

  private CellContainer myContainer;

  private CellNavigationController(final CellContainer container) {
    myContainer = container;
  }

  @Override
  public CompositeRegistration install() {
    CompositeRegistration result = super.install();
    result.add(myContainer.root.addTrait(new BaseCellTrait() {
      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        handleKeyPress(event);
        if (event.isConsumed()) return;
        super.onKeyPressed(cell, event);
      }

      @Override
      public void onMousePressed(Cell cell, MouseEvent event) {
        handleMousePress(event);
        if (event.isConsumed()) return;
        super.onMousePressed(cell, event);
      }
    }));
    return result;
  }

  @Override
  protected Property<Cell> focusedView() {
    return myContainer.focusedCell;
  }

  @Override
  protected void scrollTo(Cell view) {
    view.scrollTo();
  }

  @Override
  protected Cell root() {
    return myContainer.root;
  }

  @Override
  protected void moveCaretTo(Cell view, int offset) {
    view.get(PositionHandler.PROPERTY).caretOffset().set(offset);
  }

  @Override
  protected Selector<Cell, ReadableProperty<Integer>> caretPositionSelector() {
    return new Selector<Cell, ReadableProperty<Integer>>() {
      @Override
      public ReadableProperty<Integer> select(Cell source) {
        return source.get(PositionHandler.PROPERTY).caretOffset();
      }
    };
  }

  protected void moveToHome(Cell next) {
    if (next != null) {
      next.get(PositionHandler.PROPERTY).home();
    }
  }

  protected void moveToEnd(Cell next) {
    if (next != null) {
      next.get(PositionHandler.PROPERTY).end();
    }
  }
}
