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

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.projectional.view.TextView;

class TextCellMapper extends BaseCellMapper<TextCell, TextView> {
  TextCellMapper(TextCell source, CellToViewContext ctx) {
    super(source, new TextView(), ctx);
  }

  @Override
  void refreshProperties() {
    super.refreshProperties();

    TextCell cell = getSource();
    TextView view = getTarget();

    view.text().set(cell.text().get());
    view.caretPosition().set(cell.caretPosition().get());
    view.caretVisible().set(cell.caretVisible().get() && cell.focused().get() && cellToViewContext().containerFocused().get());
    view.textColor().set(cell.textColor().get());
    view.bold().set(cell.bold().get());
    view.selectionVisible().set(cell.selectionVisible().get() && cell.focused().get());
    view.selectionStart().set(cell.selectionStart().get());
  }
}