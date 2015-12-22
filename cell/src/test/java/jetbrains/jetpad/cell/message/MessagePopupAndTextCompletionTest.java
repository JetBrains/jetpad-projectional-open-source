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
package jetbrains.jetpad.cell.message;

import com.google.common.base.Predicates;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.text.TextEditorCompletionHandlerTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessagePopupAndTextCompletionTest extends TextEditorCompletionHandlerTest {
  @Before
  @Override
  public void init() {
    super.init();
    MessageController.install(myCellContainer);
  }

  @Test
  public void errorPopupReplacedWithCompletion() {
    MessageController.setError(getView(), "");
    assertTrue(getView().get(DecorationTrait.POPUP_ACTIVE));

    complete();
    assertNotNull(getView().get(Cell.BOTTOM_POPUP));
    assertFalse(getView().get(DecorationTrait.POPUP_ACTIVE));
    assertTrue(getController().isActive());

    escape();
    assertFalse(getController().isActive());

    assertTrue(getView().get(DecorationTrait.POPUP_ACTIVE));
    assertNotNull(getView().get(DecorationTrait.POPUP_POSITION));
  }

  @Test
  public void completeBrokenCell() {
    getView().addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysFalse()));
    complete();
    type("a");
    assertTrue(MessageController.isBroken(getView()));
    enter();
    assertEquals("a", ((TextCell) getView()).text().get());
  }
}
