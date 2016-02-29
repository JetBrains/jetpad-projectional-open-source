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
package jetbrains.jetpad.projectional.cell.mapping;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BaseProjectionalSyncToCellMapping implements Synchronizer, ToCellMapping {
  private ProjectionalRoleSynchronizer<?, ?> mySynchronizer;

  BaseProjectionalSyncToCellMapping(ProjectionalRoleSynchronizer<?, ?> synchronizer) {
    mySynchronizer = synchronizer;
  }

  @Override
  public List<Cell> getCells(Object source) {
    List<Cell> result = null;
    for (Mapper<?, ? extends Cell> mapper : mySynchronizer.getMappers()) {
      List<Cell> cells = CellProviders.doGetCells(mapper, source);
      if (!cells.isEmpty()) {
        if (result == null) {
          result = new ArrayList<>(cells.size());
        }
        result.addAll(cells);
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }

  @Override
  public Object getSource(Cell cell) {
    for (Mapper<?, ? extends Cell> mapper : mySynchronizer.getMappers()) {
      Object source = CellProviders.doGetCells(mapper, cell);
      if (source != null) return source;
    }
    return null;
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    mySynchronizer.attach(ctx);
  }

  @Override
  public void detach() {
    mySynchronizer.detach();
  }
}
