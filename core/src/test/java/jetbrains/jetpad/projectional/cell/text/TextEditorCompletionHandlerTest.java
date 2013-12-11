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
package jetbrains.jetpad.projectional.cell.text;

import jetbrains.jetpad.projectional.cell.completion.Completion;
import jetbrains.jetpad.projectional.cell.completion.CompletionHandlerTestCase;
import jetbrains.jetpad.projectional.cell.text.TextEditing;
import jetbrains.jetpad.projectional.cell.trait.BaseCellTrait;
import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.cell.TextCell;
import jetbrains.jetpad.projectional.cell.Cell;
import org.junit.Before;

public class TextEditorCompletionHandlerTest extends CompletionHandlerTestCase {
  private TextCell text = new TextCell();

  @Before
  public void init() {
    myCellContainer.root.children().add(text);

    text.text().set("");
    text.focusable().set(true);

    text.addTrait(TextEditing.textEditing());

    text.addTrait(new BaseCellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createCompletion("a", "b", "c");
        }

        return null;
      }
    });

    text.focus();
  }

  @Override
  protected Cell getView() {
    return text;
  }
}