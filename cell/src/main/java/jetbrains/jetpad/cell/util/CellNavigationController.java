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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.*;

import java.util.Stack;

import static jetbrains.jetpad.model.composite.Composites.*;

class CellNavigationController {
  static Registration install(final CellContainer container) {
    final CellNavigationController controller = new CellNavigationController(container);
    return new Registration() {
      @Override
      public void remove() {
        controller.dispose();
      }
    };
  }

  private CompositeRegistration myRegistration = new CompositeRegistration();

  private CellContainer myContainer;
  private Value<Integer> myPrevXOffset = new Value<Integer>(null);
  private Value<Boolean> myStackResetEnabled = new Value<Boolean>(true);
  private Stack<Cell> mySelectionStack = new Stack<Cell>();

  private CellNavigationController(final CellContainer container) {
    myContainer = container;

    myRegistration.add(
      selectedCaretOffset().addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Integer> event) {
          myPrevXOffset.set(null);
        }
      }),
      myContainer.focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Cell> event) {
          if (myStackResetEnabled.get()) {
            mySelectionStack.clear();
          }
        }
      }),
      myContainer.root.addTrait(new BaseCellTrait() {
        @Override
        public void onKeyPressed(Cell cell, KeyEvent event) {
          handleKeyPress(event);
          if (event.isConsumed()) return;
          super.onKeyPressed(cell, event);
        }

        @Override
        public void onMousePressed(Cell cell, MouseEvent event) {
          Cell closest = findClosest(cell, event.location());
          if (closest != null) {
            closest.focus();
            if (event.x() < closest.origin().x) {
              closest.get(PositionHandler.PROPERTY).home();
            } else {
              closest.get(PositionHandler.PROPERTY).end();
            }
            event.consume();
            return;
          }
        }
      }));
  }

  private void handleKeyPress(KeyEvent event) {
    Cell current = myContainer.focusedCell.get();
    Integer currentOffset = null;

    if (event.is(Key.UP) || event.is(Key.DOWN)) {
      currentOffset = myPrevXOffset.get();
      if (currentOffset == null) {
        currentOffset = selectedXOffset();
      }
    }

    Cell next = null;
    boolean restoreOffset = false;

    if (event.is(Key.RIGHT) || event.is(Key.TAB)) {
      next = nextFocusable(current);
      moveToHome(next);
    } else if (event.is(Key.RIGHT, ModifierKey.ALT) || event.is(Key.RIGHT, ModifierKey.CONTROL)) {
      next = nextFocusable(current);
      moveToEnd(next);
    } else if (event.is(Key.LEFT) || event.is(Key.TAB, ModifierKey.SHIFT)) {
      next = prevFocusable(current);
      moveToEnd(next);
    } else if (event.is(Key.LEFT, ModifierKey.ALT) || event.is(Key.LEFT, ModifierKey.CONTROL)) {
      next = prevFocusable(current);
      moveToHome(next);
    } else if (event.is(Key.UP)) {
      next = upperFocusable(current, currentOffset);
      restoreOffset = true;
    } else if (event.is(Key.DOWN)) {
      next = lowerFocusable(current, currentOffset);
      restoreOffset = true;
    } else if (event.is(Key.HOME) || event.is(Key.LEFT, ModifierKey.META)) {
      next = Composites.homeElement(current);
      moveToHome(next);
    } else if (event.is(Key.END) || event.is(Key.RIGHT, ModifierKey.META)) {
      next = Composites.endElement(current);
      moveToEnd(next);
    } else if (event.is(Key.UP, ModifierKey.ALT)) {
      Cell focusableParent = Composites.focusableParent(current);
      if (focusableParent != null) {
        mySelectionStack.push(current);
        next = focusableParent;
        myStackResetEnabled.set(false);
      }
    } else if (event.is(Key.DOWN, ModifierKey.ALT)) {
      if (mySelectionStack.isEmpty()) {
        next = Composites.firstFocusable(current, false);
      } else {
        next = mySelectionStack.pop();
      }
      myStackResetEnabled.set(false);
    }
    if (next != null) {
      myContainer.focusedCell.set(next);
      next.scrollTo();

      if (restoreOffset) {
        next.get(PositionHandler.PROPERTY).caretOffset().set(currentOffset - next.origin().x);
        myPrevXOffset.set(currentOffset);
      }

      event.consume();
    }
    myStackResetEnabled.set(true);
  }

  private void moveToHome(Cell next) {
    if (next != null) {
      next.get(PositionHandler.PROPERTY).home();
    }
  }

  private void moveToEnd(Cell next) {
    if (next != null) {
      next.get(PositionHandler.PROPERTY).end();
    }
  }

  private int selectedXOffset() {
    return selectedCaretOffset().get() + myContainer.focusedCell.get().origin().x;
  }

  private ReadableProperty<Integer> selectedCaretOffset() {
    return Properties.select(
      Properties.select(myContainer.focusedCell, new Selector<Cell, ReadableProperty<PositionHandler>>() {
        @Override
        public ReadableProperty<PositionHandler> select(Cell input) {
          if (input == null) return null;

          return Properties.<PositionHandler>constant(input.get(PositionHandler.PROPERTY));
        }
      }),
      new Selector<PositionHandler, ReadableProperty<Integer>>() {
        @Override
        public ReadableProperty<Integer> select(PositionHandler input) {
          if (input == null) return null;
          return input.caretOffset();
        }
      });
  }

  public void dispose() {
    myRegistration.remove();
  }
}
