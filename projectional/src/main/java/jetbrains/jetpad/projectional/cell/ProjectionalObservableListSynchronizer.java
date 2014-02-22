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
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.projectional.generic.CollectionEditor;
import jetbrains.jetpad.projectional.generic.Role;

import java.util.ArrayList;
import java.util.List;

class ProjectionalObservableListSynchronizer<ContextT, SourceItemT> extends BaseProjectionalSynchronizer<ObservableList<SourceItemT>, ContextT, SourceItemT> {
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
  protected Registration registerChild(SourceItemT child, final Cell childCell) {
    return childCell.addTrait(new CellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        if (!(cell instanceof TextCell)) {
          return new CellTrait[] { CompletionSupport.trait() };
        }
        return super.getBaseTraits(cell);
      }

      @Override
      public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
        if (event.isConsumed()) return;

        try {
          boolean isMultiSelection = getSelectedItems().size() > 1;
          if (!isMultiSelection) {
            keyPressedInChild(event);
          } else {
            if (isDeleteEvent(event)) {
              clear(getSelectedItems());
              event.consume();
            }
          }
        } finally {
          if (event.isConsumed()) {
            scrollToSelection();
          }
        }

        super.onKeyPressedLowPriority(cell, event);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              return getCurrentChildCompletion().get(cp);
            }
          };
        }

        return super.get(cell, spec);
      }
    });
  }

  private void keyPressedInChild(KeyEvent event) {
    new CollectionEditor<SourceItemT, Cell>(mySource, childCells(), canCreateNewItem()) {
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
        CellActions.toFirstFocusable(childCells().get(index)).run();
      }

      @Override
      protected void selectEnd(int index) {
        CellActions.toLastFocusable(childCells().get(index)).run();
      }

      @Override
      protected void selectPlaceholder() {
        getOnLastItemDeleted().run();
      }
    }.handleKey(currentCell(), event);
  }

  private void scrollToSelection() {
    getTarget().cellContainer().get().focusedCell.get().scrollTo();
  }

  private CompletionSupplier getCurrentChildCompletion() {
    return createCompletion(new Role<SourceItemT>() {
      @Override
      public SourceItemT get() {
        return mySource.get(childCells().indexOf(currentCell()));
      }

      @Override
      public Runnable set(SourceItemT target) {
        int index = childCells().indexOf(currentCell());
        mySource.set(index, target);
        return childCells().get(index).get(ProjectionalSynchronizers.ON_CREATE);
      }
    });
  }

  @Override
  protected Runnable insertItems(List<SourceItemT> items) {
    int index = childCells().indexOf(currentCell());
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
        Cell target = childCells().get(index);
        CellActions.toFirstFocusable(target).run();
      } else {
        Cell target = childCells().get(index - 1);
        CellActions.toLastFocusable(target).run();
      }
    }
  }

  private boolean isEmpty(int index) {
    return Cells.isEmpty(childCells().get(index));
  }
}