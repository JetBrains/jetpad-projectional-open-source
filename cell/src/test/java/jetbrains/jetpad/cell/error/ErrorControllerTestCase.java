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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.event.MouseEvent;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class ErrorControllerTestCase extends EditingTestCase {
  protected Cell cell;
  private Registration registration;

  @Before
  public void init() {
    registration = ErrorController.install(myCellContainer);
    cell = doCreateCell();
    myCellContainer.root.children().add(cell);
  }

  @After
  public void cleanup() {
    registration.remove();
  }

  protected Cell doCreateCell() {
    return new TextCell("abc");
  }

  protected void showErrorPopup() {
    setError();
    mouseEntered();
    assertDecorationPopupVisible(cell, true);
  }

  protected void mouseEntered() {
    myCellContainer.mouseEntered(new MouseEvent(cell.getBounds().center()));
  }

  protected void mouseLeft() {
    myCellContainer.mouseLeft(new MouseEvent(0, 0));
  }

  protected void mouseMoved() {
    myCellContainer.mouseMoved(new MouseEvent(0, 0));
  }

  protected void assertDecorationPopupVisible(Cell c, boolean visible) {
    assertTrue(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
    Cell popup = c.get(ErrorDecorationTrait.POPUP_POSITION);
    assertNotNull(popup);
    assertEquals(visible, popup.get(Cell.VISIBLE));
  }

  protected void setError() {
    ErrorController.setError(cell, "");
  }

  protected void removeError() {
    ErrorController.setError(cell, null);
  }

  protected String getPopupMessage() {
    return ((TextCell) cell.get(ErrorDecorationTrait.POPUP_POSITION)).text().get();
  }
}
