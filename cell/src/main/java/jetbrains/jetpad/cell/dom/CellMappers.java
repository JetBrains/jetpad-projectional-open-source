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
package jetbrains.jetpad.cell.dom;

import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.indent.IndentRootCell;

class CellMappers {
  static BaseCellMapper<?> createMapper(Cell source, CellToDomContext ctx) {
    if (source instanceof TextCell) {
      return new TextCellMapper((TextCell) source, ctx);
    }

    if (source instanceof HorizontalCell) {
      return new HorizontalCellMapper((HorizontalCell) source, ctx  );
    }

    if (source instanceof VerticalCell) {
      return new VerticalCellMapper((VerticalCell) source, ctx);
    }

    if (source instanceof ScrollCell) {
      return new ScrollCellMapper((ScrollCell) source, ctx);
    }

    if (source instanceof IndentRootCell) {
      return new IndentRootCellMapper((IndentRootCell) source, ctx);
    }

    if (source instanceof RootCell) {
      return new RootCellMapper((RootCell) source, ctx);
    }

    return null;
  }

}