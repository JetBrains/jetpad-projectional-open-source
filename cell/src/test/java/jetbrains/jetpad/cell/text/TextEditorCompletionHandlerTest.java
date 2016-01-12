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
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.CompletionHandlerTestCase;
import jetbrains.jetpad.cell.indent.IndentCell;
import org.junit.Before;
import org.junit.Test;

public class TextEditorCompletionHandlerTest extends CompletionHandlerTestCase {
  private TextCell text;

  @Before
  public void init() {
    text = new TextCell("");
    text.focusable().set(true);
    text.addTrait(TextEditing.textEditing());
    text.addTrait(createCompletionTrait("a", "b", "c"));
    myCellContainer.root.children().add(text);
    text.focus();
  }

  @Override
  protected Cell getView() {
    return text;
  }

  @Test
  public void indentContainer() {
    TextCell text = new TextCell("");
    text.focusable().set(true);
    text.addTrait(TextEditing.textEditing());
    text.addTrait(createCompletionTrait("a", "b", "c"));

    IndentCell indent = new IndentCell();
    indent.children().add(text);
    myCellContainer.root.children().add(indent);
    text.focus();

    type("d");
    complete();
    assertHasBottomPopup(text);

    escape();
    assertNoBottomPopup(text);
  }
}