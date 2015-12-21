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
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorPopupsTest extends ErrorControllerTestCase {

  @Test
  public void setErrorPopup() {
    final Value<Boolean> errorPopupChanged = new Value<>(false);
    cell.addListener(new CellAdapter() {
      @Override
      public void onPropertyChanged(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (Cell.isPopupProp(prop) && cell.get(ErrorDecorationTrait.POPUP_ACTIVE)) {
          errorPopupChanged.set(true);
        }
      }
    });
    setError();
    assertTrue(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertNotNull(cell.get(ErrorDecorationTrait.POPUP_POSITION));
    assertTrue(errorPopupChanged.get());
  }

  @Test
  public void removeErrorPopup() {
    setError();
    removeError();
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertNull(cell.get(ErrorDecorationTrait.POPUP_POSITION));
  }

  @Test
  public void showBroken() {
    ErrorController.setBroken(cell, "");
    mouseEntered();
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void showWarning() {
    ErrorController.setWarning(cell, "");
    mouseEntered();
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void mouseEvents() {
    showErrorPopup();

    mouseLeft();
    assertDecorationPopupVisible(cell, false);
  }

  @Test
  public void errorSetWithMouseOver() {
    mouseEntered();

    setError();
    assertDecorationPopupVisible(cell, false);
    mouseMoved();
    assertDecorationPopupVisible(cell, true);

    mouseLeft();
    assertDecorationPopupVisible(cell, false);
  }

  @Test
  public void errorSetAndRemovedWithMouseOver() {
    mouseEntered();
    setError();
    assertDecorationPopupVisible(cell, false);
    mouseMoved();
    assertDecorationPopupVisible(cell, true);

    removeError();
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertNull(cell.get(ErrorDecorationTrait.POPUP_POSITION));

    mouseLeft();
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
    assertNull(cell.get(ErrorDecorationTrait.POPUP_POSITION));
  }

  @Test
  public void mouseLeftWhenErrorPopupHidden() {
    mouseEntered();
    setError();
    assertDecorationPopupVisible(cell, false);
    mouseMoved();
    assertDecorationPopupVisible(cell, true);


    TextCell child = new TextCell("child");
    cell.children().add(child);
    child.bottomPopup().set(new TextCell());
    assertDecorationPopupVisible(cell, false);

    mouseLeft();
    assertDecorationPopupVisible(cell, false);

    mouseEntered();
    assertDecorationPopupVisible(cell, false);

    child.bottomPopup().set(null);
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void setOtherPopup() {
    cell.set(ErrorDecorationTrait.POPUP_POSITION, new TextCell());
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));
  }

  @Test
  public void parentErrorHintReplacedByChilds() {
    TextCell child = new TextCell("child");
    cell.children().add(child);

    showErrorPopup();

    child.frontPopup().set(new TextCell());
    assertDecorationPopupVisible(cell, false);

    child.bottomPopup().set(new TextCell());
    assertDecorationPopupVisible(cell, false);

    cell.children().remove(0);
    assertDecorationPopupVisible(cell, true);

    mouseMoved();
    assertDecorationPopupVisible(cell, true);

    mouseLeft();
    assertDecorationPopupVisible(cell, false);
  }

  @Test
  public void detachedPopupVisibility() {
    TextCell child = new TextCell("child");
    cell.children().add(child);

    showErrorPopup();

    TextCell popup = new TextCell();
    child.frontPopup().set(popup);
    assertDecorationPopupVisible(cell, false);

    popup.visible().set(false);
    assertDecorationPopupVisible(cell, true);

    popup.visible().set(true);
    assertDecorationPopupVisible(cell, false);

    child.frontPopup().set(null);
    assertDecorationPopupVisible(cell, true);

    popup.visible().set(true);
    popup.visible().set(false);
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void removeChildWithPopups() {
    showErrorPopup();
    TextCell child = new TextCell("child");
    child.frontPopup().set(new TextCell("front"));
    cell.children().add(child);
    assertDecorationPopupVisible(cell, false);

    child.bottomPopup().set(new TextCell("bottom"));
    cell.children().remove(0);

    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void childPopupReplacement() {
    showErrorPopup();

    TextCell child = new TextCell("child");
    cell.children().add(child);
    assertDecorationPopupVisible(cell, true);

    child.frontPopup().set(new TextCell("1"));
    assertDecorationPopupVisible(cell, false);

    child.frontPopup().set(new TextCell("2"));
    assertDecorationPopupVisible(cell, false);
  }

  @Test
  public void changeErrorStateWhileOtherPopupShown() {
    HorizontalCell popup = new HorizontalCell();
    cell.set(ErrorDecorationTrait.POPUP_POSITION, popup);

    ErrorController.setError(cell, "a");
    assertSame(popup, cell.get(ErrorDecorationTrait.POPUP_POSITION));
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));

    cell.set(ErrorDecorationTrait.POPUP_POSITION, null);
    assertDecorationPopupVisible(cell, false);
  }

  @Test
  public void changeErrorStateWhileChildPopupShown() {
    HorizontalCell child = new HorizontalCell();
    cell.children().add(child);
    HorizontalCell popup = new HorizontalCell();
    child.frontPopup().set(popup);

    ErrorController.setError(cell, "a");
    assertDecorationPopupVisible(cell, false);
    mouseEntered();
    assertDecorationPopupVisible(cell, false);

    child.frontPopup().set(null);
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void deepChildWithErrorPopup() {
    TextCell child1 = new TextCell("child1");
    TextCell child2 = new TextCell("child2");
    TextCell child3 = new TextCell("child3");

    showErrorPopup();
    cell.children().add(child1);
    assertDecorationPopupVisible(cell, true);

    child1.children().add(child2);
    assertDecorationPopupVisible(cell, true);

    child2.children().add(child3);
    assertDecorationPopupVisible(cell, true);

    setError();
    ErrorController.setError(child3, "");
    mouseMoved();

    assertDecorationPopupVisible(child3, true);
    assertDecorationPopupVisible(cell, false);

    ErrorController.setError(child3, null);
    mouseMoved();
    assertDecorationPopupVisible(cell, true);

    cell.children().remove(0);
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void deepChildWithPopup() {
    TextCell child1 = new TextCell("child1");
    TextCell child2 = new TextCell("child2");
    TextCell child3 = new TextCell("child3");
    TextCell popup = new TextCell("popup");

    showErrorPopup();
    cell.children().add(child1);
    assertDecorationPopupVisible(cell, true);

    child1.children().add(child2);
    assertDecorationPopupVisible(cell, true);

    child2.children().add(child3);
    assertDecorationPopupVisible(cell, true);

    child3.bottomPopup().set(popup);
    assertDecorationPopupVisible(cell, false);

    child3.bottomPopup().set(null);
    assertDecorationPopupVisible(cell, true);

    cell.children().remove(0);
    assertDecorationPopupVisible(cell, true);
  }
}