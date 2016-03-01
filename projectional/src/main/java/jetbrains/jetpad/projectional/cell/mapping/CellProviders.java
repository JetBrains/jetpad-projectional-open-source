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
import jetbrains.jetpad.mapper.ByTargetIndex;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.composite.HasParent;
import jetbrains.jetpad.model.composite.TreePath;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;

import java.util.*;

public class CellProviders {

  public static List<Cell> sort(List<Cell> cells) {
    final Map<Cell, TreePath<Cell>> paths = new HashMap<>();
    for (Cell cell : cells) {
      paths.put(cell, new TreePath<>(cell));
    }
    Collections.sort(cells, new Comparator<Cell>() {
      @Override
      public int compare(Cell cell1, Cell cell2) {
        return paths.get(cell1).compareTo(paths.get(cell2));
      }
    });
    return cells;
  }

  public static <SourceT extends HasParent<?>> CellProvider<SourceT> fromMapper(final Mapper<?, ?> mapper) {
    MappingContext mappingContext = mapper.getMappingContext();
    ByTargetIndex byTargetIndex = mappingContext.get(ByTargetIndex.KEY);
    if (byTargetIndex == null) {
      byTargetIndex = new ByTargetIndex(mappingContext);
      mappingContext.put(ByTargetIndex.KEY, byTargetIndex);
    }
    final ByTargetIndex index = byTargetIndex;
    return new CellProvider<SourceT>() {
      @Override
      public List<Cell> getCells(SourceT source) {
        return lookupCells(source, source, mapper);
      }

      @Override
      public SourceT getSource(Cell cell) {
        return lookupSource(cell, index);
      }
    };
  }

  private static <SourceT> SourceT lookupSource(Cell cell, ByTargetIndex byTargetIndex) {
    for (Cell c = cell; c != null; c = c.getParent()) {
      Collection<Mapper<?, ?>> mappers = byTargetIndex.getMappers(c);
      if (!mappers.isEmpty()) {
        Mapper<?, ?> mapper = mappers.iterator().next();
        SourceT source = (SourceT) doGetSource(mapper, cell);
        return source == null ? (SourceT) getCellMappingSource(mapper) : source;
      }
    }
    return null;
  }

  private static Object doGetSource(Mapper<?, ?> mapper, Cell actualTarget) {
    if (mapper.getTarget() == actualTarget) {
      return getCellMappingSource(mapper);
    }
    for (Synchronizer sync : mapper.synchronizers()) {
      Object source = getSource(sync, actualTarget);
      if (source != null) return source;
    }
    return null;
  }

  private static Object getSource(Synchronizer sync, Cell target) {
    if (sync instanceof ToCellMapping) {
      return ((ToCellMapping) sync).getSource(target);
    }
    if (sync instanceof ProjectionalRoleSynchronizer) {
      return getSourceFromProjectionalSynchronizer((ProjectionalRoleSynchronizer<?, ?>) sync, target);
    }
    return null;
  }

  private static Object getSourceFromProjectionalSynchronizer(ProjectionalRoleSynchronizer<?, ?> sync, Cell cell) {
    for (Mapper<?, ? extends Cell> mapper : sync.getMappers()) {
      Object source = doGetSource(mapper, cell);
      if (source != null) return source;
    }
    return null;
  }

  private static List<Cell> lookupCells(HasParent source, HasParent actualSource, Mapper<?, ?> toSearchAt) {
    Set<Mapper<? super HasParent, ?>> mappers = toSearchAt.getMappingContext().getMappers(toSearchAt, source);
    if (!mappers.isEmpty()) {
      List<Cell> cells = new ArrayList<>(mappers.size());
      for (Mapper<?, ?> mapper : mappers) {
        if (mapper.getTarget() instanceof Cell) {
          cells.addAll(doGetCells((Mapper<?, ? extends Cell>) mapper, actualSource));
        }
      }
      return cells;
    }
    HasParent parent = source.getParent();
    if (parent == null) {
      return Collections.emptyList();
    }
    return lookupCells(parent, actualSource, toSearchAt);
  }

  private static List<Cell> doGetCells(Mapper<?, ? extends Cell> mapper, Object actualSource) {
    if (getCellMappingSource(mapper) == actualSource) {
      return Collections.singletonList(mapper.getTarget());
    }
    List<Cell> result = null;
    for (Synchronizer sync : mapper.synchronizers()) {
      List<Cell> cells = getCells(sync, actualSource);
      if (cells != null && !cells.isEmpty()) {
        if (result == null) {
          result = new ArrayList<>(cells.size());
        }
        result.addAll(cells);
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }

  private static Object getCellMappingSource(Mapper<?, ?> mapper) {
    return mapper instanceof HasParentWrapper ? ((HasParentWrapper) mapper).getCellMappingSource() : mapper.getSource();
  }

  private static List<Cell> getCells(Synchronizer sync, Object source) {
    if (sync instanceof ToCellMapping) {
      return ((ToCellMapping) sync).getCells(source);
    }
    if (sync instanceof ProjectionalRoleSynchronizer) {
      return getCellsFromProjectionalSynchronizer((ProjectionalRoleSynchronizer<?, ?>) sync, source);
    }
    return null;
  }

  private static List<Cell> getCellsFromProjectionalSynchronizer(ProjectionalRoleSynchronizer<?, ?> sync, Object source) {
    List<Cell> result = null;
    for (Mapper<?, ? extends Cell> mapper : sync.getMappers()) {
      List<Cell> cells = doGetCells(mapper, source);
      if (!cells.isEmpty()) {
        if (result == null) {
          result = new ArrayList<>(cells.size());
        }
        result.addAll(cells);
      }
    }
    return result;
  }
}
