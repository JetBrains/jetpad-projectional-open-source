/*
 * Copyright 2012-2014 JetBrains s.r.o
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
  private static final CellTraitPropertySpec<SelectionSupport<?>> SELECTION_SUPPORT = new CellTraitPropertySpec<>("selectionSupport");

  private ObservableList<ItemT> mySelectedItems = new ObservableArrayList<>();
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

    Cell current = myTarget.cellContainer().get().focusedCell.get();
    while (current != null) {
      Cell parent = current.parent().get();
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
    Cell focusedCell = myTarget.cellContainer().get().focusedCell.get();

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
      }
    });
  }

  public void clearSelection() {
    changeSelection(new Runnable() {
      @Override
      public void run() {
        mySelectedItems.clear();
      }
    });
  }

  private void handleFocusLost(FocusEvent event) {
    if (myChangingSelection) return;

    mySelectedItems.clear();
  }

  private void handleFocusGain(FocusEvent event) {
    Cell newValue = event.getNewValue();
    Cell normalizedNewValue = newValue != null ? normalizeFocus(newValue) : null;
    if (normalizedNewValue == null || !myTargetList.contains(normalizedNewValue)) return;

    if (myChangingSelection) return;

    if (!Cells.isLeaf(newValue)) {
      int index = myTargetList.indexOf(normalizedNewValue);
      select(mySource.get(index), mySource.get(index));
    } else {
      mySelectedItems.clear();
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

          if (!Positions.isEndPosition(currentCell) && !isCurrentCompletelySelected()) {
            if (!mySelectedItems.contains(currentItem)) {
              mySelectedItems.add(currentItem);
              focusAndScrollTo(currentIndex, false).run();
            } else {
              mySelectedItems.remove(currentItem);
              if (currentIndex == myTargetList.size() - 1) {
                focusAndScrollTo(currentIndex, false).run();
              } else {
                focusAndScrollTo(currentIndex + 1, true).run();
              }
            }
            event.consume();
            return;
          }

          if (!mySelectedItems.contains(currentItem) && Positions.isHomePosition(currentCell) && Positions.isEndPosition(currentCell)) {
            mySelectedItems.add(currentItem);
          }

          if (currentIndex == myTargetList.size() - 1) return;

          ItemT nextItem = mySource.get(currentIndex + 1);

          if (mySelectedItems.contains(nextItem)) {
            mySelectedItems.remove(currentItem);
            focusAndScrollTo(currentIndex + 1, true).run();
          } else {
            mySelectedItems.add(nextItem);
            focusAndScrollTo(currentIndex + 1, false).run();
          }
          event.consume();
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

          if (!Positions.isHomePosition(currentCell) && !isCurrentCompletelySelected()) {
            if (!mySelectedItems.contains(currentItem)) {
              mySelectedItems.add(0, currentItem);
              focusAndScrollTo(currentIndex, true).run();
            } else {
              mySelectedItems.remove(currentItem);
              if (currentIndex == 0) {
                focusAndScrollTo(currentIndex, true).run();
              } else {
                focusAndScrollTo(currentIndex - 1, false).run();
              }
            }
            event.consume();
            return;
          }

          if (!mySelectedItems.contains(currentItem) && Positions.isHomePosition(currentCell) && Positions.isEndPosition(currentCell)) {
            mySelectedItems.add(currentItem);
          }

          if (currentIndex == 0) return;

          ItemT prevItem = mySource.get(currentIndex - 1);

          if (mySelectedItems.contains(prevItem)) {
            mySelectedItems.remove(currentItem);
            focusAndScrollTo(currentIndex - 1, false).run();
          } else {
            mySelectedItems.add(0, prevItem);
            focusAndScrollTo(currentIndex - 1, true).run();
          }
          event.consume();
        }
      });
    }
  }

  private boolean isLowerPrioritySelection() {
    Cell current = myTarget;
    while (true) {
      Cell parent = current.parent().get();
      if (parent == null) return false;
      SelectionSupport<?> selection = parent.get(SELECTION_SUPPORT);
      if (selection != null) {
        return !selection.selection().isEmpty();
      }
      if (!Composites.isLastChild(current) && !Composites.isFirstChild(current)) return false;
      current = parent;
    }
  }

  private Cell normalizeFocus(Cell cell) {
    Cell current = cell;
    while (true) {
      if (isSingleChild(current)) {
        Cell parent = current.parent().get();
        if (parent == null || parent == myTarget) return current;
        current = parent;
      } else {
        return current;
      }
    }
  }

  private boolean isSingleChild(Cell cell) {
    if (cell.parent().get() == null) return false;
    Cell parent = cell.parent().get();
    List<Cell> siblings = parent.children();
    return siblings.contains(cell) && siblings.size() == 1;
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
}