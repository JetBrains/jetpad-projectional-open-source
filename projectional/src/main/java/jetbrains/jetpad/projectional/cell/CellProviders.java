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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.ByTargetIndex;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.composite.HasParent;
import jetbrains.jetpad.model.composite.TreePath;

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

  public static <SrcT extends HasParent<?>> CellProvider<SrcT> fromMapper(final Mapper<?, ?> mapper) {
    final ByTargetIndex index = new ByTargetIndex(mapper.getMappingContext());

    return new CellProvider<SrcT>() {
      @Override
      public void dispose() {
        index.dispose();
      }

      @Override
      public List<Cell> getCells(SrcT source) {
        return lookupCells(source, mapper);
      }

      @Override
      public SrcT getSource(Cell cell) {
        return lookupSource(cell, index);
      }
    };
  }

  private static <SrcT> SrcT lookupSource(Cell cell, ByTargetIndex byTargetIndex) {
    for (Cell c = cell; c != null; c = c.getParent()) {
      Collection<Mapper<?, ?>> mappers = byTargetIndex.getMappers(c);
      if (!mappers.isEmpty()) {
        return (SrcT) doGetSource(mappers.iterator().next(), cell);
      }
    }
    return null;
  }

  static Object doGetSource(Mapper<?, ?> mapper, Cell actualTarget) {
    if (!(mapper instanceof ToCellMapping)) {
      return mapper.getSource();
    }
    return ((ToCellMapping) mapper).getSource(actualTarget);
  }

  private static List<Cell> lookupCells(HasParent<?> source, Mapper<?, ?> toSearchAt) {
    if (toSearchAt instanceof ToCellMapping) {
      List<Cell> fromSynchronizers = ((ToCellMapping<?>) toSearchAt).getCells(source);
      if (!fromSynchronizers.isEmpty()) return fromSynchronizers;
    }
    return lookupCells(source, source, toSearchAt);
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

  static List<Cell> doGetCells(Mapper<?, ? extends Cell> mapper, Object actualSource) {
    if (mapper.getSource() == actualSource) {
      return Collections.singletonList(mapper.getTarget());
    }
    if (!(mapper instanceof ToCellMapping)) {
      return Collections.emptyList();
    }
    return ((ToCellMapping<?>) mapper).getCells(actualSource);
  }
}
