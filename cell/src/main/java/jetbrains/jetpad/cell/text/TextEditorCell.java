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
package jetbrains.jetpad.cell.text;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.values.Color;

public abstract class TextEditorCell implements TextEditor {
  public static final CellPropertySpec<String> TEXT = new CellPropertySpec<>("text", "");
  public static final CellPropertySpec<Color> TEXT_COLOR = new CellPropertySpec<>("textColor", Color.BLACK);
  public static final CellPropertySpec<Boolean> CARET_VISIBLE = new CellPropertySpec<>("caretVisible", false);
  public static final CellPropertySpec<Integer> CARET_POSITION = new CellPropertySpec<>("caretPosition", 0);
  public static final CellPropertySpec<Boolean> SELECTION_VISIBLE = new CellPropertySpec<>("selectionVisible", false);
  public static final CellPropertySpec<Integer> SELECTION_START = new CellPropertySpec<>("selectionStart", 0);

  private Cell myCell;

  public TextEditorCell(Cell cell) {
    myCell = cell;
  }

  public Cell getCell() {
    return myCell;
  }
}
