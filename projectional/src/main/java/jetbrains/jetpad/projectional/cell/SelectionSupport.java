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
package jetbrains.jetpad.projectional.cell;

import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

import java.util.List;

public class SelectionSupport<ItemT> {
  public static final CellTraitPropertySpec<Boolean> LOGICAL_SINGLE_CELL_CONTAINER = new CellTraitPropertySpec<>("logicalSingleCellContainer", false);
  private static final CellTraitPropertySpec<SelectionSupport<?>> SELECTION_SUPPORT = new CellTraitPropertySpec<>("selectionSupport");

  private ObservableList<ItemT> mySelectedItems = new ObservableArrayList<>();
  private Direction myDirection;
  private boolean myChangingSelection;
  private List<ItemT> mySource;
  private Cell myTarget;
  private List<Cell> myTargetList;

  public SelectionSupport(
      List<ItemT> source,
      Cell target,
      List<Cell> targetList) {
    mySource = source;
    myTarget = target;
    myTargetList = targetList;

    myTarget.addTrait(new CellTrait() {
      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        super.onFocusGained(cell, event);
        handleFocusGain(event);
      }

      @Override
      public void onFocusLost(Cell cell, FocusEvent event) {
        super.onFocusLost(cell, event);
        handleFocusLost(event);
      }

      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        handleTargetKeyPress(event);
        if (event.isConsumed()) return;

        super.onKeyPressed(cell, event);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == SELECTION_SUPPORT) return SelectionSupport.this;
        return super.get(cell, spec);
      }
    });
  }

  public ObservableList<ItemT> selection() {
    return mySelectedItems;
  }

  public Cell currentCell() {
    if (mySource.isEmpty()) return null;

    Cell current = myTarget.getContainer().focusedCell.get();
    while (current != null) {
      Cell parent = current.getParent();
      if (parent == myTarget) {
        return current;
      }
      current = parent;
    }
    return null;
  }

  public boolean isCurrentCompletelySelected() {
    Cell current = currentCell();
    return current.focused().get() && !Cells.isLeaf(current);
  }

  public void select(ItemT from, ItemT to) {
    final int start = mySource.indexOf(from);
    final int end = mySource.indexOf(to);
    Cell focusedCell = myTarget.getContainer().focusedCell.get();

    if (start == -1 || end == -1) {
      throw new IllegalArgumentException();
    }
    if (start > end) {
      throw new IllegalArgumentException();
    }
    if (!Composites.isDescendant(myTarget, focusedCell)) {
      throw new IllegalStateException();
    }

    int currentIndex = myTargetList.indexOf(currentCell());
    if (currentIndex < start || currentIndex > end) {
      throw new IllegalArgumentException();
    }

    runSelectionAction(new Runnable() {
      @Override
      public void run() {
        mySelectedItems.clear();
        for (int i = start; i <= end; i++) {
          mySelectedItems.add(mySource.get(i));
        }
        myDirection = Direction.FORWARD;
      }
    });
  }

  public void clearSelection() {
    changeSelection(new Runnable() {
      @Override
      public void run() {
        mySelectedItems.clear();
        myDirection = null;
      }
    });
  }

  private void handleFocusLost(FocusEvent event) {
    if (myChangingSelection) return;

    clearSelection();
  }

  private void handleFocusGain(FocusEvent event) {
    Cell newValue = event.getNewValue();
    Cell expanded = newValue != null ? expand(newValue) : null;
    if (expanded == null || !myTargetList.contains(expanded)) {
      expand(newValue);
      return;
    }

    if (myChangingSelection) return;

    if (!Cells.isLeaf(newValue)) {
      int index = myTargetList.indexOf(expanded);
      select(mySource.get(index), mySource.get(index));
    } else {
      clearSelection();
    }
  }

  private void handleTargetKeyPress(final KeyEvent event) {
    if (event.is(KeyStrokeSpecs.SELECT_AFTER)) {
      runSelectionAction(new Runnable() {
        @Override
        public void run() {
          if (isLowerPrioritySelection()) return;

          Cell currentCell = currentCell();
          if (currentCell == null) return;

          int currentIndex = myTargetList.indexOf(currentCell);
          ItemT currentItem = mySource.get(currentIndex);
          boolean consumed = false;

          if (!Positions.isEndPosition(currentCell) && !isCurrentCompletelySelected()) {
            if (!mySelectedItems.contains(currentItem)) {
              mySelectedItems.add(currentItem);
              resetFocusAndScrollTo(currentIndex, false).run();
            } else {
              if (myDirection == Direction.FORWARD) {
                resetFocusAndScrollTo(currentIndex, false).run();
              } else {
                mySelectedItems.remove(currentItem);
                if (currentIndex == myTargetList.size() - 1) {
                  resetFocusAndScrollTo(currentIndex, false).run();
                } else {
                  resetFocusAndScrollTo(currentIndex + 1, true).run();
                }
              }
            }

            consumed = true;
          } else {
            int focusIndex = -1;
            boolean focusOnFirst = true;

            if (!mySelectedItems.contains(currentItem) && Positions.isHomePosition(currentCell) && Positions.isEndPosition(currentCell)) {
              mySelectedItems.add(currentItem);
              focusIndex = currentIndex;
              focusOnFirst = false;
              consumed = true;
            }

            if (currentIndex < myTargetList.size() - 1) {
              ItemT nextItem = mySource.get(currentIndex + 1);
              if (mySelectedItems.contains(nextItem)) {
                mySelectedItems.remove(currentItem);
                focusIndex = currentIndex + 1;
              } else {
                mySelectedItems.add(nextItem);
                focusIndex = currentIndex + 1;
                focusOnFirst = false;
              }
              consumed = true;
            }

            if (focusIndex != -1) {
              resetFocusAndScrollTo(focusIndex, focusOnFirst).run();
            }
          }


          if (consumed) {
            if (myDirection == null) {
              myDirection = Direction.FORWARD;
            }
            event.consume();
          }
        }
      });
    }

    if (event.is(KeyStrokeSpecs.SELECT_BEFORE)) {
      runSelectionAction(new Runnable() {
        @Override
        public void run() {
          if (isLowerPrioritySelection()) return;

          Cell currentCell = currentCell();
          if (currentCell == null) return;

          int currentIndex = myTargetList.indexOf(currentCell);
          ItemT currentItem = mySource.get(currentIndex);
          boolean consumed = false;

          if (!Positions.isHomePosition(currentCell) && !isCurrentCompletelySelected()) {
            if (!mySelectedItems.contains(currentItem)) {
              mySelectedItems.add(0, currentItem);
              resetFocusAndScrollTo(currentIndex, true).run();
            } else {
              if (myDirection == Direction.BACKWARD) {
                resetFocusAndScrollTo(currentIndex, true).run();
              } else {
                mySelectedItems.remove(currentItem);
                if (currentIndex == 0) {
                  resetFocusAndScrollTo(currentIndex, true).run();
                } else {
                  resetFocusAndScrollTo(currentIndex - 1, false).run();
                }
              }
            }
            consumed = true;
          } else {
            int focusIndex = -1;
            boolean focusOnFirst = true;

            if (!mySelectedItems.contains(currentItem) && Positions.isHomePosition(currentCell) && Positions.isEndPosition(currentCell)) {
              mySelectedItems.add(0, currentItem);
              focusIndex = currentIndex;
              consumed = true;
            }

            if (currentIndex > 0) {
              ItemT prevItem = mySource.get(currentIndex - 1);

              if (mySelectedItems.contains(prevItem)) {
                mySelectedItems.remove(currentItem);
                focusIndex = currentIndex - 1;
                focusOnFirst = false;
              } else {
                mySelectedItems.add(0, prevItem);
                focusIndex = currentIndex - 1;
              }
              consumed = true;
            }

            if (focusIndex != -1) {
              resetFocusAndScrollTo(focusIndex, focusOnFirst).run();
            }
          }

          if (consumed) {
            if (myDirection == null) {
              myDirection = Direction.BACKWARD;
            }
            event.consume();
          }
        }
      });
    }
  }

  private boolean isLowerPrioritySelection() {
    Cell current = myTarget;
    while (true) {
      Cell parent = current.getParent();
      if (parent == null) return false;
      SelectionSupport<?> selection = parent.get(SELECTION_SUPPORT);
      if (selection != null && !selection.selection().isEmpty()) {
        return true;
      }
      if (!Composites.isLastChild(current) && !Composites.isFirstChild(current)) return false;
      current = parent;
    }
  }

  private Cell expand(Cell cell) {
    Cell current = cell;
    while (true) {
      Cell parent = current.getParent();
      if (parent == null) return current;
      if (isSingleChild(current)) {
        if (parent == myTarget) return current;
        current = parent;
      } else {
        return current;
      }
    }
  }

  private boolean isSingleChild(Cell cell) {
    if (cell.getParent() == null) return false;
    Cell parent = cell.getParent();
    if (parent.get(LOGICAL_SINGLE_CELL_CONTAINER)) return true;
    List<Cell> siblings = parent.children();
    return siblings.contains(cell) && siblings.size() == 1;
  }

  private Runnable resetFocusAndScrollTo(int index, boolean first) {
    return Runnables.seq(
        new Runnable() {
          @Override
          public void run() {
            // Dropping the focus clears all other selections
            myTarget.getContainer().focusedCell.set(null);
          }
        },
        focusAndScrollTo(index, first)
    );
  }

  protected Runnable focusAndScrollTo(int index, boolean first) {
    final Cell child = myTargetList.get(index);
    return Runnables.seq(
      first ? CellActions.toFirstFocusable(child) : CellActions.toLastFocusable(child),
      new Runnable() {
        @Override
        public void run() {
          child.scrollTo();
        }
      }
    );
  }

  private void runSelectionAction(Runnable r) {
    if (myChangingSelection) {
      throw new IllegalStateException();
    }
    changeSelection(r);
  }

  private void changeSelection(Runnable r) {
    myChangingSelection = true;
    try {
      r.run();
    } finally {
      myChangingSelection = false;
    }
  }

  private static enum Direction {
    FORWARD, BACKWARD;
  }
}