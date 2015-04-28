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
package jetbrains.jetpad.projectional.cell;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.trait.*;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.projectional.generic.CollectionEditor;
import jetbrains.jetpad.projectional.generic.Role;

import java.util.ArrayList;
import java.util.List;

class ProjectionalObservableListSynchronizer<ContextT, SourceItemT> extends BaseProjectionalSynchronizer<ObservableList<SourceItemT>, ContextT, SourceItemT> {
  static final CellTraitPropertySpec<ItemHandler> ITEM_HANDLER = new CellTraitPropertySpec<ItemHandler>("itemHandler");

  private ObservableList<SourceItemT> mySource;

  ProjectionalObservableListSynchronizer(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      ObservableList<SourceItemT> source,
      Cell target,
      List<Cell> targetList,
      MapperFactory<SourceItemT, Cell> factory) {
    super(mapper, source, target, targetList, factory);
    mySource = source;
  }

  @Override
  protected RoleSynchronizer<SourceItemT, Cell> createSubSynchronizer(Mapper<?, ?> mapper, ObservableList<SourceItemT> source, List<Cell> target, MapperFactory<SourceItemT, Cell> factory) {
    return Synchronizers.forObservableRole(mapper, source, target, factory);
  }

  @Override
  protected Registration doRegisterChild(final SourceItemT child, final Cell childCell) {
    return new CompositeRegistration(
      childCell.addTrait(new DerivedCellTrait() {
        @Override
        protected CellTrait getBase(Cell cell) {
          if (!(cell instanceof TextCell)) {
            return CompletionSupport.trait();
          }
          return CellTrait.EMPTY;
        }

        @Override
        public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
          try {
            if (getSelectedItems().isEmpty()) {
              keyPressedInChild(event);
            }
          } finally {
            if (event.isConsumed() && cell.isAttached()) {
              scrollToSelection();
            }
          }

          super.onKeyPressedLowPriority(cell, event);
        }

        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.COMPLETION) {
            return getCurrentChildCompletion();
          }

          if (spec == ITEM_HANDLER) {
            return new ItemHandler() {
              @Override
              public Runnable addEmptyAfter() {
                int index = mySource.indexOf(child);
                final SourceItemT newItem = newItem();
                mySource.add(index + 1, newItem);
                return selectOnCreation(index + 1);
              }
            };
          }

          return super.get(cell, spec);
        }

        @Override
        public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
          if (spec == Cells.BECAME_EMPTY && cell.get(ProjectionalSynchronizers.DELETE_ON_EMPTY)) {
            int index = getChildCells().indexOf(cell);
            if (index == -1) return;
            clear(mySource.subList(index, index + 1));
            event.consume();
            return;
          }

          super.onCellTraitEvent(cell, spec, event);
        }

        @Override
        public void onKeyTyped(Cell cell, KeyEvent event) {
          if (getSeparator() != null && getSeparator() == event.getKeyChar()) {
            int index = getChildCells().indexOf(cell);
            SourceItemT newItem = newItem();
            mySource.add(index + 1, newItem);
            selectOnCreation(index + 1).run();
            event.consume();
          }

          super.onKeyTyped(cell, event);
        }
      })
    );
  }

  private void keyPressedInChild(KeyEvent event) {
    new CollectionEditor<SourceItemT, Cell>(mySource, getChildCells(), canCreateNewItem()) {
      @Override
      protected SourceItemT newItem() {
        return ProjectionalObservableListSynchronizer.this.newItem();
      }

      @Override
      protected boolean isEmpty(Cell item) {
        return Cells.isEmpty(item);
      }

      @Override
      protected boolean isHome(Cell item) {
        return Positions.isHomePosition(item);
      }

      @Override
      protected boolean isEnd(Cell item) {
        return Positions.isEndPosition(item);
      }

      @Override
      protected void selectOnCreation(int index) {
        ProjectionalObservableListSynchronizer.this.selectOnCreation(index).run();
      }

      @Override
      protected void selectHome(int index) {
        CellActions.toFirstFocusable(getChildCells().get(index)).run();
      }

      @Override
      protected void selectEnd(int index) {
        CellActions.toLastFocusable(getChildCells().get(index)).run();
      }

      @Override
      protected void selectPlaceholder() {
        getOnLastItemDeleted().run();
      }

      @Override
      protected boolean addAfterParent() {
        Cell current = getTarget();
        Cell nextVisible = nextCell(current);

        while (current != null && nextCell(current) == nextVisible) {
          final ItemHandler handler = current.get(ITEM_HANDLER);
          if (handler != null) {
            handler.addEmptyAfter().run();
            return true;
          }
          current = current.getParent();
        }

        return false;
      }

      private boolean isIgnored(Cell cell) {
        if (cell.get(ProjectionalSynchronizers.IGNORED_ON_BOUNDARY)) {
          return true;
        }
        if (cell.getParent() != null) {
          return isIgnored(cell.getParent());
        }
        return false;
      }

      private Cell nextCell(Cell root) {
        for (Cell c : Composites.nextNavOrder(root)) {
          if (Composites.isVisible(c) && !isIgnored(c)) return c;
        }
        return null;
      }

    }.handleKey(currentCell(), event);
  }


  private CompletionSupplier getCurrentChildCompletion() {
    return createCompletion(new Role<SourceItemT>() {
      @Override
      public SourceItemT get() {
        return mySource.get(getChildCells().indexOf(currentCell()));
      }

      @Override
      public Runnable set(SourceItemT target) {
        int index = getChildCells().indexOf(currentCell());
        mySource.set(index, target);
        return getChildCells().get(index).get(ProjectionalSynchronizers.ON_CREATE);
      }
    });
  }

  @Override
  protected Runnable insertItems(List<SourceItemT> items) {
    int index = getChildCells().indexOf(currentCell());
    if (index == -1) {
      mySource.addAll(items);
      return selectOnCreation(items.size() - 1);
    }

    if (Positions.isHomePosition(currentCell())) {
      mySource.addAll(index, items);
      if (!isEmpty(index)) {
        return selectOnCreation(index);
      }
      return Runnables.EMPTY;
    } else {
      mySource.addAll(index + 1, items);
      return selectOnCreation(index + items.size());
    }
  }

  @Override
  protected void clear(List<SourceItemT> items) {
    int firstIndex = mySource.indexOf(items.get(0));
    for (SourceItemT item : new ArrayList<>(items)) {
      mySource.remove(item);
    }
    selectAfterClear(firstIndex);
  }

  private void selectAfterClear(int index) {
    if (mySource.isEmpty()) {
      getOnLastItemDeleted().run();
    } else {
      if (index < mySource.size()) {
        Cell target = getChildCells().get(index);
        CellActions.toFirstFocusable(target).run();
      } else {
        Cell target = getChildCells().get(index - 1);
        CellActions.toLastFocusable(target).run();
      }
    }
  }

  private boolean isEmpty(int index) {
    return Cells.isEmpty(getChildCells().get(index));
  }

  static interface ItemHandler {
    Runnable addEmptyAfter();
  }
}