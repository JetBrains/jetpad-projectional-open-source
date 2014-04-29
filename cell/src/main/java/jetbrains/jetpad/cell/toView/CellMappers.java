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
package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.view.ViewCell;

class CellMappers {
  static BaseCellMapper<?, ?> create(Cell cell, CellToViewContext ctx) {
    if (cell instanceof HorizontalCell) {
      return new HorizontalCellMapper((HorizontalCell) cell, ctx);
    }

    if (cell instanceof VerticalCell) {
      return new VerticalCellMapper((VerticalCell) cell, ctx);
    }

    if (cell instanceof ScrollCell) {
      return new ScrollCellMapper((ScrollCell) cell, ctx);
    }

    if (cell instanceof TextCell) {
      return new TextCellMapper((TextCell) cell, ctx);
    }

    if (cell instanceof ImageCell) {
      return new ImageCellMapper((ImageCell) cell, ctx);
    }

    if (cell instanceof RootCell) {
      return new RootCellMapper((RootCell) cell, ctx);
    }

    if (cell instanceof IndentCell) {
      IndentCell indentCell = (IndentCell) cell;
      if (!indentCell.isRootIndent()) {
        throw new IllegalStateException();
      }
      return new IndentRootCellMapper(indentCell, ctx);
    }

    if (cell instanceof ViewCell) {
      return new ViewCellMapper((ViewCell) cell, ctx);
    }

    return new DefaultCellMapper(cell, ctx);
  }
}