/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.test.CellPart;
import jetbrains.jetpad.cell.indent.test.IndentPart;
import jetbrains.jetpad.cell.indent.test.NewLinePart;

import java.util.Arrays;

class CellUtil {
  static String toString(Cell cell) {
    StringBuilder builder = new StringBuilder();
    toString(cell, builder);
    return builder.toString();
  }

  private static void toString(Cell cell, StringBuilder result) {
    if (cell instanceof TextCell) {
      result.append("'").append(((TextCell) cell).text().get()).append("'");
    } else {
      result.append("[");
      boolean first = true;
      for (Cell child : cell.children()) {
        if (first) {
          first = false;
        } else {
          result.append(", ");
        }
        toString(child, result);
      }
      result.append("]");
    }
  }

  static IndentPart text(String text) {
    return new CellPart(new TextCell(text));
  }

  static IndentPart composite(String text) {
    CellPart result = new CellPart(new TextCell(text));
    result.children().add(text("c1"));
    result.children().add(text("c2"));
    return result;
  }

  static IndentPart list(IndentPart... parts) {
    return list(false, parts);
  }

  static IndentPart list(boolean indented, IndentPart... parts) {
    IndentPart result = new IndentPart(indented);
    result.children.addAll(Arrays.asList(parts));
    return result;
  }

  static NewLinePart newLine() {
    return new NewLinePart();
  }
}