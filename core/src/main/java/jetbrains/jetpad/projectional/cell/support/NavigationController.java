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
package jetbrains.jetpad.projectional.cell.support;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.*;
import jetbrains.jetpad.projectional.cell.trait.BaseCellTrait;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.CellContainer;
import jetbrains.jetpad.projectional.cell.util.Cells;

import java.util.Stack;

import static jetbrains.jetpad.projectional.cell.util.Cells.*;

class NavigationController {
  static Registration install(CellContainer container) {
    final Cell cell = container.root;

    final Value<Integer> prevXOffset = new Value<Integer>(null);
    final Value<Boolean> stackResetEnabled = new Value<Boolean>(true);
    final Stack<Cell> selectionStack = new Stack<Cell>();
    final ReadableProperty<Integer> selectionXOffset = selectedXOffset(container);
    final ReadableProperty<Cell> focusedCell = container.focusedCell;
    return new CompositeRegistration(
      focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Cell> event) {
          Cell oldCell = event.getOldValue();
          if (oldCell != null) {
            oldCell.highlighted().set(false);
          }

          Cell newCell = event.getNewValue();
          if (newCell != null) {
            newCell.highlighted().set(true);
          }
        }
      }),
      selectedCaretOffset(container).addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Integer> event) {
          prevXOffset.set(null);
        }
      }),
      focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Cell> event) {
          if (stackResetEnabled.get()) {
            selectionStack.clear();
          }
        }
      }),

      cell.addTrait(new BaseCellTrait() {
        @Override
        public void onKeyPressed(Cell cell, KeyEvent event) {
          Cell current = cell.container().focusedCell.get();
          Integer currentOffset = null;

          if (event.is(Key.UP) || event.is(Key.DOWN)) {
            currentOffset = prevXOffset.get();
            if (currentOffset == null) {
              currentOffset = selectionXOffset.get();
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
            next = homeElement(current);
            moveToHome(next);
          } else if (event.is(Key.END) || event.is(Key.RIGHT, ModifierKey.META)) {
            next = endElement(current);
            moveToEnd(next);
          } else if (event.is(Key.UP, ModifierKey.ALT)) {
            Cell focusableParent = focusableParent(current);
            if (focusableParent != null) {
              selectionStack.push(current);
              next = focusableParent;
              stackResetEnabled.set(false);
            }
          } else if (event.is(Key.DOWN, ModifierKey.ALT)) {
            if (selectionStack.isEmpty()) {
              next = Cells.firstFocusable(current, false);
            } else {
              next = selectionStack.pop();
            }
            stackResetEnabled.set(false);
          }
          if (next != null) {
            cell.container().focusedCell.set(next);
            next.scrollTo();

            if (restoreOffset) {
              next.get(PositionHandler.PROPERTY).caretOffset().set(currentOffset - next.origin().x);
              prevXOffset.set(currentOffset);
            }

            event.consume();
          }
          stackResetEnabled.set(true);
          if (event.isConsumed()) return;


          super.onKeyPressed(cell, event);
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

        @Override
        public void onMousePressed(Cell cell, MouseEvent event) {
          Cell closest = findClosestView(cell, event.x(), event.y());
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

  private static Cell upperFocusable(Cell c, int xOffset) {
    Cell current = prevFocusable(c);
    Cell bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isAbove(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (isAbove(current, c)) {
        bestMatch = current;
      }

      current = prevFocusable(current);
    }

    return bestMatch;
  }

  private static Cell lowerFocusable(Cell c, int xOffset) {
    Cell current = nextFocusable(c);
    Cell bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isBelow(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (isBelow(current, c)) {
        bestMatch = current;
      }

      current = nextFocusable(current);
    }

    return bestMatch;
  }

  private static double distanceTo(Cell c, int x) {
    return c.getBounds().distance(new Vector(x, c.origin().y));
  }

  private static ReadableProperty<Integer> selectedXOffset(CellContainer container) {
    ReadableProperty<Integer> selectionX = Properties.select(container.focusedCell, new Selector<Cell, ReadableProperty<Integer>>() {
      @Override
      public ReadableProperty<Integer> select(final Cell input) {
        return new BaseReadableProperty<Integer>() {
          @Override
          public Integer get() {
            return input.origin().x;
          }

          @Override
          public Registration addHandler(EventHandler<? super PropertyChangeEvent<Integer>> handler) {
            //we can't watch to relayout events, so x coordinate, might change, if relayout is performed. We ignore such changes here
            return Registration.EMPTY;
          }

          @Override
          public String getPropExpr() {
            return "selectionX";
          }
        };
      }
    });
    return Properties.add(selectedCaretOffset(container), selectionX);
  }

  private static ReadableProperty<Integer> selectedCaretOffset(CellContainer container) {
    return Properties.select(
      Properties.select(container.focusedCell, new Selector<Cell, ReadableProperty<PositionHandler>>() {
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

  private static Cell findClosestView(Cell current, int x, int y) {
    if (!current.visible().get()) return null;

    Range<Integer> range = Ranges.closed(current.origin().y, current.origin().y + current.dimension().y);
    if (!range.contains(y)) {
      return null;
    }
    Cell result = null;
    int distance = Integer.MAX_VALUE;
    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;

      Cell closest = findClosestView(child, x, y);
      if (closest == null) continue;
      int newDistance = (int) closest.getBounds().distance(new Vector(x, y));

      if (newDistance < distance) {
        result = closest;
        distance = newDistance;
      }
    }

    if (result == null && current.focusable().get()) {
      return current;
    }

    return result;
  }
}
