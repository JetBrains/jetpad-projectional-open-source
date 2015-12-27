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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

public abstract class MessageControllerTestCase extends EditingTestCase {
  protected Cell cell;
  private Registration registration;

  @Before
  public void init() {
    registration = MessageController.install(myCellContainer);
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
    setError(cell);
    mouseEntered(cell);
    assertDecorationPopupVisible(cell, true);
  }

  protected void mouseEntered(Cell c) {
    Rectangle bounds = c.getBounds();
    myCellContainer.mouseEntered(new MouseEvent(bounds.origin.add(new Vector(bounds.dimension.x / 2, bounds.dimension.y))));
  }

  protected void mouseLeft() {
    myCellContainer.mouseLeft(new MouseEvent(0, 0));
  }

  protected void mouseMoved() {
    myCellContainer.mouseMoved(new MouseEvent(1, 1));
  }

  protected void assertDecorationPopupVisible(Cell c, boolean visible) {
    assertTrue(c.get(MessageTrait.POPUP_ACTIVE));
    Cell popup = c.get(MessageTrait.POPUP_POSITION);
    assertNotNull(popup);
    assertEquals(visible, popup.get(Cell.VISIBLE));
  }

  protected void setError(Cell c) {
    MessageController.setError(c, "");
  }

  protected void removeError(Cell c) {
    MessageController.setError(c, null);
  }

  protected String getPopupMessage() {
    return ((TextCell) cell.get(MessageTrait.POPUP_POSITION)).text().get();
  }
}