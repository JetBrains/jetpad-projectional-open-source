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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.generic.ItemsSplitterJoiner;
import jetbrains.jetpad.projectional.generic.Role;

import java.util.List;

class ProjectionalPropertySynchronizer<ContextT, SourceItemT> extends BaseProjectionalSynchronizer<Property<SourceItemT>, ContextT, SourceItemT> {
  private Property<SourceItemT> mySource;

  ProjectionalPropertySynchronizer(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      Property<SourceItemT> source,
      Cell target,
      MapperFactory<SourceItemT, Cell> factory) {
    super(mapper, source, target, target.children(), factory);
    mySource = source;
  }

  @Override
  protected RoleSynchronizer<SourceItemT, Cell> createSubSynchronizer(Mapper<?, ?> mapper, Property<SourceItemT> source, final List<Cell> target, MapperFactory<SourceItemT, Cell> factory) {
    return Synchronizers.forSingleRole(mapper, source, new WritableProperty<Cell>() {
      @Override
      public void set(Cell value) {
        target.clear();
        if (value != null) {
          target.add(value);
        }
      }
    }, factory);
  }

  @Override
  protected Registration doRegisterChild(SourceItemT child, Cell childCell) {
    return childCell.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        if (!(cell instanceof TextCell)) {
          return CompletionSupport.trait();
        }
        return CellTrait.EMPTY;
      }

      @Override
      public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
        handleItemKeyPress(event, cell);
        if (event.isConsumed()) return;

        super.onKeyPressed(cell, event);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return getCompletion();
        }

        return super.get(cell, spec);
      }

      @Override
      public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
        if (spec == Cells.BECAME_EMPTY && cell.get(ProjectionalSynchronizers.DELETE_ON_EMPTY)) {
          itemBecameEmpty();
          event.consume();
          return;
        }

        super.onCellTraitEvent(cell, spec, event);
      }
    });
  }

  @Override
  protected void clear(List<SourceItemT> items) {
    if (items.isEmpty()) return;
    deleteItem();
  }

  @Override
  protected Runnable insertItems(List<SourceItemT> items) {
    mySource.set(items.get(0));
    return selectOnCreation(0);
  }

  @Override
  protected boolean isMultiItemPasteSupported() {
    return false;
  }

  private void itemBecameEmpty() {
    deleteItem();
  }

  private CompletionSupplier getCompletion() {
    return createCompletion(new Role<SourceItemT>() {
      @Override
      public SourceItemT get() {
        return mySource.get();
      }

      @Override
      public Runnable set(SourceItemT target) {
        mySource.set(target);
        return selectOnCreation(0);
      }
    });
  }

  private void handleItemKeyPress(KeyEvent event, Cell cell) {
    if (isSimpleDeleteEvent(event, cell, true) && getForDeletion().get() == null) {
      getForDeletion().set(mySource.get());
      event.consume();
      return;
    }


    if (isDeleteEvent(event, cell)) {
      deleteItem();
      event.consume();
    }
  }

  private void deleteItem() {
    mySource.set(null);
    getOnLastItemDeleted().run();
    if (getTarget().get(ProjectionalSynchronizers.DELETE_ON_EMPTY)) {
      getTarget().dispatch(new Event(), Cells.BECAME_EMPTY);
    }
  }

  @Override
  public void setItemsSplitterJoiner(ItemsSplitterJoiner<SourceItemT, Cell> splitterJoiner) {
    throw new UnsupportedOperationException();
  }
}