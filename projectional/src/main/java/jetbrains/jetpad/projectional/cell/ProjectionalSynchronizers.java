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

import com.google.common.base.Function;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;

import java.util.List;

public class ProjectionalSynchronizers {
  public static final CellTraitPropertySpec<Runnable> ON_CREATE = new CellTraitPropertySpec<>("onCreate", new Function<Cell, Runnable>() {
    @Override
    public Runnable apply(Cell input) {
      return CellActions.toLastFocusable(input);
    }
  });
  public static final CellTraitPropertySpec<Boolean> DELETE_ON_EMPTY = new CellTraitPropertySpec<>("deleteOnEmpty", false);


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
    return new ProjectionalObservableListSynchronizer<>(mapper, source, target, targetList, factory);
  }

  public static <ContextT, SourceT extends ContextT> ProjectionalRoleSynchronizer<ContextT, SourceT> forSingleRole(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      Property<SourceT> source, Cell target, MapperFactory<SourceT, Cell> factory) {
    return new ProjectionalPropertySynchronizer<>(mapper, source, target, factory);
  }

  public static boolean isAdd(KeyEvent event) {
    return event.is(KeyStrokeSpecs.INSERT_BEFORE) || event.is(KeyStrokeSpecs.INSERT_AFTER);
  }
}