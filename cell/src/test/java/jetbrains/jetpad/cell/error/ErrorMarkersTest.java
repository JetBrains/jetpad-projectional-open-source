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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorMarkersTest extends EditingTestCase {
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
        if (Cell.isPopupProp(prop) && cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE)) {
          errorPopupChanged.set(true);
        }
      }
    });
    cell.set(Cell.HAS_ERROR, true);
    assertTrue(cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE));
    assertNotNull(cell.get(ErrorMarkers.ERROR_POPUP_POSITION));
    assertTrue(errorPopupChanged.get());
  }

  @Test
  public void removeErrorPopup() {
    cell.set(Cell.HAS_ERROR, true);
    cell.set(Cell.HAS_ERROR, false);
    assertFalse(cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE));
    assertNull(cell.get(ErrorMarkers.ERROR_POPUP_POSITION));
  }

  @Test
  public void mouseEvents() {
    showErrorPopup(cell);

    myCellContainer.mouseLeft(new MouseEvent(new Vector(0, 0)));
    assertErrorPopupVisible(cell, false);
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
  public void childPopupReplaced() {
    showErrorPopup(cell);

    TextCell child = new TextCell("child");
    cell.children().add(child);
    assertErrorPopupVisible(cell, true);

    child.frontPopup().set(new TextCell("1"));
    assertErrorPopupVisible(cell, false);

    child.frontPopup().set(new TextCell("2"));
    assertErrorPopupVisible(cell, false);
  }

  private void showErrorPopup(Cell c) {
    c.set(Cell.HAS_ERROR, true);
    myCellContainer.mouseEntered(new MouseEvent(c.getBounds().center()));
    assertErrorPopupVisible(c, true);
  }

  private void assertErrorPopupVisible(Cell c, boolean visible) {
    assertTrue(cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE));
    Cell popup = c.get(ErrorMarkers.ERROR_POPUP_POSITION);
    assertNotNull(popup);
    assertEquals(visible, popup.get(Cell.VISIBLE));
  }
}