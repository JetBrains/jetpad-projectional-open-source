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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.Selector;

public class CellProperties {
  public static ReadableProperty<Boolean> focusIn(final Cell cell) {
    final ReadableProperty<Cell> focusedCell = Properties.select(cell.cellContainer(), new Selector<CellContainer, ReadableProperty<Cell>>() {
      @Override
      public ReadableProperty<Cell> select(CellContainer source) {
        return source.focusedCell;
      }
    });

    return new DerivedProperty<Boolean>(focusedCell) {
      @Override
      public Boolean doGet() {
        Cell focused = focusedCell.get();
        if (focused == null) return false;
        return Composites.isDescendant(cell, focused);
      }
    };
  }
}