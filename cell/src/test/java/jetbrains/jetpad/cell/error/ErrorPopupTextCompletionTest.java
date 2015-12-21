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
package jetbrains.jetpad.cell.error;

import com.google.common.base.Predicates;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.text.TextEditorCompletionHandlerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class  ErrorPopupTextCompletionTest extends TextEditorCompletionHandlerTest {
  private Registration myRegistration;

  @Before
  @Override
  public void init() {
    myRegistration = ErrorController.install(myCellContainer);
    super.init();
  }

  @After
  public void cleanup() {
    myRegistration.remove();
  }

  @Test
  public void errorPopupReplacedWithCompletion() {
    ErrorController.setError(getView(), "");
    assertTrue(getView().get(ErrorDecorationTrait.POPUP_ACTIVE));

    complete();
    assertNotNull(getView().get(ErrorDecorationTrait.POPUP_POSITION));
    assertFalse(getView().get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertTrue(getController().isActive());

    escape();
    assertFalse(getController().isActive());

    assertTrue(getView().get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertNotNull(getView().get(ErrorDecorationTrait.POPUP_POSITION));
  }

  @Test
  public void completeBrokenCell() {
    getView().addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysFalse()));
    complete();
    type("a");
    assertTrue(ErrorController.isBroken(getView()));
    enter();
    assertEquals("a", ((TextCell) getView()).text().get());
  }
}
