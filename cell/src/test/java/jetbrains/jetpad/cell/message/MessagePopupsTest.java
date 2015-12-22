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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessagePopupsTest extends EditingTestCase {
  private TextCell cell;

  @Before
  public void init() {
    cell = new TextCell("abc");
    ErrorMarkers.install(cell);
    myCellContainer.root.children().add(cell);
  }

  @Test
  public void setErrorPopup() {
    final Value<Boolean> errorPopupChanged = new Value<>(false);
    cell.addListener(new CellAdapter() {
      @Override
      public void onPropertyChanged(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (Cell.isPopupProp(prop) && cell.get(ErrorMarkers.POPUP_ACTIVE)) {
          errorPopupChanged.set(true);
        }
      }
    });
    setError(cell);
    assertTrue(cell.get(ErrorMarkers.POPUP_ACTIVE));
    assertNotNull(cell.get(ErrorMarkers.POPUP_POSITION));
    assertTrue(errorPopupChanged.get());
  }

  @Test
  public void removeErrorPopup() {
    setError(cell);
    removeError(cell);
    assertFalse(cell.get(ErrorMarkers.POPUP_ACTIVE));
    assertNull(cell.get(ErrorMarkers.POPUP_POSITION));
  }

  @Test
  public void mouseEvents() {
    showErrorPopup(cell);

    mouseLeft();
    assertErrorPopupVisible(cell, false);
  }

  @Test
  public void errorSetWithMouseOver() {
    mouseEntered(cell);

    setError(cell);
    assertErrorPopupVisible(cell, false);
    mouseMoved();
    assertErrorPopupVisible(cell, true);

    mouseLeft();
    assertErrorPopupVisible(cell, false);
  }

  @Test
  public void errorSetAndRemovedWithMouseOver() {
    mouseEntered(cell);
    setError(cell);
    assertErrorPopupVisible(cell, false);
    mouseMoved();
    assertErrorPopupVisible(cell, true);

    removeError(cell);
    assertFalse(cell.get(ErrorMarkers.POPUP_ACTIVE));
    assertNull(cell.get(ErrorMarkers.POPUP_POSITION));

    mouseLeft();
    assertFalse(cell.get(ErrorMarkers.POPUP_ACTIVE));
    assertNull(cell.get(ErrorMarkers.POPUP_POSITION));
  }

  @Test
  public void mouseLeftWhenErrorPopupHidden() {
    mouseEntered(cell);
    setError(cell);
    assertErrorPopupVisible(cell, false);
    mouseMoved();
    assertErrorPopupVisible(cell, true);


    TextCell child = new TextCell("child");
    cell.children().add(child);
    child.bottomPopup().set(new TextCell());
    assertErrorPopupVisible(cell, false);

    mouseLeft();
    assertErrorPopupVisible(cell, false);

    mouseEntered(cell);
    assertErrorPopupVisible(cell, false);

    child.bottomPopup().set(null);
    assertErrorPopupVisible(cell, true);
  }

  @Test
  public void setOtherPopup() {
    cell.set(ErrorMarkers.POPUP_POSITION, new TextCell());
    assertFalse(cell.get(ErrorMarkers.POPUP_ACTIVE));
  }

  @Test
  public void parentErrorHintReplacedByChilds() {
    TextCell child = new TextCell("child");
    cell.children().add(child);

    showErrorPopup(cell);

    child.frontPopup().set(new TextCell());
    assertErrorPopupVisible(cell, false);

    child.bottomPopup().set(new TextCell());
    assertErrorPopupVisible(cell, false);

    cell.children().remove(0);
    assertErrorPopupVisible(cell, true);

    mouseMoved();
    assertErrorPopupVisible(cell, true);

    mouseLeft();
    assertErrorPopupVisible(cell, false);
  }

  @Test
  public void detachedPopupVisibility() {
    TextCell child = new TextCell("child");
    cell.children().add(child);

    showErrorPopup(cell);

    TextCell popup = new TextCell();
    child.frontPopup().set(popup);
    assertErrorPopupVisible(cell, false);

    popup.visible().set(false);
    assertErrorPopupVisible(cell, true);

    popup.visible().set(true);
    assertErrorPopupVisible(cell, false);

    child.frontPopup().set(null);
    assertErrorPopupVisible(cell, true);

    popup.visible().set(true);
    popup.visible().set(false);
    assertErrorPopupVisible(cell, true);
  }

  @Test
  public void removeChildWithPopups() {
    showErrorPopup(cell);
    TextCell child = new TextCell("child");
    child.frontPopup().set(new TextCell("front"));
    cell.children().add(child);
    assertErrorPopupVisible(cell, false);

    child.bottomPopup().set(new TextCell("bottom"));
    cell.children().remove(0);

    assertErrorPopupVisible(cell, true);
  }

  @Test
  public void childPopupReplacement() {
    showErrorPopup(cell);

    TextCell child = new TextCell("child");
    cell.children().add(child);
    assertErrorPopupVisible(cell, true);

    child.frontPopup().set(new TextCell("1"));
    assertErrorPopupVisible(cell, false);

    child.frontPopup().set(new TextCell("2"));
    assertErrorPopupVisible(cell, false);
  }

  @Test
  public void changeErrorStateWhileOtherPopupShown() {
    HorizontalCell popup = new HorizontalCell();
    cell.set(ErrorMarkers.POPUP_POSITION, popup);

    setError(cell);
    assertSame(popup, cell.get(ErrorMarkers.POPUP_POSITION));
    assertFalse(cell.get(ErrorMarkers.POPUP_ACTIVE));

    cell.set(ErrorMarkers.POPUP_POSITION, null);
    assertErrorPopupVisible(cell, false);
  }

  @Test
  public void changeErrorStateWhileChildPopupShown() {
    HorizontalCell child = new HorizontalCell();
    cell.children().add(child);
    HorizontalCell popup = new HorizontalCell();
    child.frontPopup().set(popup);

    setError(cell);
    assertErrorPopupVisible(cell, false);
    mouseEntered(cell);
    assertErrorPopupVisible(cell, false);

    child.frontPopup().set(null);
    assertErrorPopupVisible(cell, true);
  }

  @Test
  public void deepChildWithPopup() {
    TextCell child1 = new TextCell("child1");
    TextCell child2 = new TextCell("child2");
    TextCell child3 = new TextCell("child3");
    TextCell popup = new TextCell("popup");

    showErrorPopup(cell);
    cell.children().add(child1);
    assertErrorPopupVisible(cell, true);

    child1.children().add(child2);
    assertErrorPopupVisible(cell, true);

    child2.children().add(child3);
    assertErrorPopupVisible(cell, true);

    child3.bottomPopup().set(popup);
    assertErrorPopupVisible(cell, false);

    child3.bottomPopup().set(null);
    assertErrorPopupVisible(cell, true);

    cell.children().remove(0);
    assertErrorPopupVisible(cell, true);
  }

  private void setError(Cell c) {
    c.set(Cell.HAS_ERROR, true);
  }

  private void removeError(Cell c) {
    c.set(Cell.HAS_ERROR, false);
  }

  private void mouseEntered(Cell c) {
    myCellContainer.mouseEntered(new MouseEvent(c.getBounds().center()));
  }

  private void mouseMoved() {
    myCellContainer.mouseMoved(new MouseEvent(0, 0));
  }

  private void mouseLeft() {
    myCellContainer.mouseLeft(new MouseEvent(0, 0));
  }

  private void showErrorPopup(Cell c) {
    setError(c);
    mouseEntered(c);
    assertErrorPopupVisible(c, true);
  }

  private void assertErrorPopupVisible(Cell c, boolean visible) {
    assertTrue(cell.get(ErrorMarkers.POPUP_ACTIVE));
    Cell popup = c.get(ErrorMarkers.POPUP_POSITION);
    assertNotNull(popup);
    assertEquals(visible, popup.get(Cell.VISIBLE));
  }
}