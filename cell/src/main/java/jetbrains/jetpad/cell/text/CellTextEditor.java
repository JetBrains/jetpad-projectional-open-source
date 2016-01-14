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

public abstract class CellTextEditor implements TextEditor {
  private Cell myCell;

  public CellTextEditor(Cell cell) {
    myCell = cell;
  }

  public Cell getCell() {
    return myCell;
  }

  @Override
  public boolean isFirstAllowed() {
    return myCell.get(TextEditing.FIRST_ALLOWED);
  }

  @Override
  public boolean isLastAllowed() {
    return myCell.get(TextEditing.LAST_ALLOWED);
  }
}
