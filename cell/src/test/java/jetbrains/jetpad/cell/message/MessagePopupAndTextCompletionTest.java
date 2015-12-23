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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.text.TextEditorCompletionHandlerTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessagePopupAndTextCompletionTest extends TextEditorCompletionHandlerTest {
  @Before
  @Override
  public void init() {
    super.init();
    ErrorMarkers.install(getView());
  }

  @Test
  public void errorPopupReplacedWithCompletion() {
    getView().hasError().set(true);
    assertTrue(getView().get(ErrorMarkers.POPUP_ACTIVE));

    complete();
    assertNotNull(getView().get(Cell.BOTTOM_POPUP));
    assertFalse(getView().get(ErrorMarkers.POPUP_ACTIVE));
    assertTrue(getController().isActive());

    escape();
    assertFalse(getController().isActive());

    assertTrue(getView().get(ErrorMarkers.POPUP_ACTIVE));
    assertNotNull(getView().get(ErrorMarkers.POPUP_POSITION));
  }
}
