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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.mappers.CellMapper;
import jetbrains.jetpad.cell.toUtil.AncestorUtil;
import jetbrains.jetpad.cell.toUtil.CounterUtil;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class IndentUtil {

  public static void updateCounters(Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> event,
                                    final Mapper<IndentCell, ?> parent) {
    iterateLeaves(cell, new Handler<Cell>() {
      @Override
      public void handle(Cell item) {
        CellMapper mapper = (CellMapper) parent.getDescendantMapper(item);
        if (mapper == null) {
          throw new IllegalStateException();
        }
        if (CounterUtil.update(mapper, prop, event)) {
          mapper.refreshProperties();
        }
      }
    });
  }

  public static void updateBackground(Cell cell, final Mapper<IndentCell, ?> parent) {
    iterateLeaves(cell, new Handler<Cell>() {
      @Override
      public void handle(Cell item) {
        CellMapper mapper = (CellMapper) parent.getDescendantMapper(item);
        mapper.setAncestorBackground(AncestorUtil.getAncestorBackground(parent.getSource(), item));
        mapper.refreshProperties();
      }
    });
  }

  private static void iterateLeaves(Cell cell, Handler<Cell> handler) {
    for (Cell child : cell.children()) {
      if (!Composites.isVisible(child)) continue;
      if (child instanceof IndentCell) {
        iterateLeaves(child, handler);
      } else {
        handler.handle(child);
      }
    }
  }
}
