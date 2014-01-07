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

import com.google.common.base.Function;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.action.CellAction;
import jetbrains.jetpad.cell.action.CellActions;

import java.util.List;

public class ProjectionalSynchronizers {
  public static final CellTraitPropertySpec<CellAction> ON_CREATE = new CellTraitPropertySpec<CellAction>("onCreate", new Function<Cell, CellAction>() {
    @Override
    public CellAction apply(Cell input) {
      return CellActions.toLastFocusable(input);
    }
  });
  public static final CellTraitPropertySpec<Boolean> DELETE_ON_EMPTY = new CellTraitPropertySpec<Boolean>("deleteOnEmpty", false);


  public static <ContextT, SourceT> ProjectionalRoleSynchronizer<ContextT, SourceT> forRole(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      ObservableList<SourceT> source, Cell target,
      MapperFactory<SourceT, Cell> factory) {
    return forRole(mapper, source, target, target.children(), factory);
  }

  public static <ContextT, SourceT> ProjectionalRoleSynchronizer<ContextT, SourceT> forRole(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      ObservableList<SourceT> source, Cell target, List<Cell> targetList,
      MapperFactory<SourceT, Cell> factory) {
    return new ProjectionalObservableListSynchronizer<ContextT, SourceT>(mapper, source, target, targetList, factory);
  }

  public static <ContextT, SourceT extends ContextT> ProjectionalRoleSynchronizer<ContextT, SourceT> forSingleRole(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      Property<SourceT> source, Cell target, MapperFactory<SourceT, Cell> factory) {
    return new ProjectionalPropertySynchronizer<ContextT, SourceT>(mapper, source, target, factory);
  }

  public static boolean isAdd(KeyEvent event) {
    return event.is(KeyStrokeSpecs.INSERT_BEFORE) || event.is(KeyStrokeSpecs.INSERT_AFTER);
  }
}