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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.text.TextEditing;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CellNavigationControllerTest extends EditingTestCase {
  private Cell c1 = new HorizontalCell();
  private TextCell c11 = view(true);
  private Cell c12 = view(false);
  private Cell c13 = view(true);
  private TextCell c14 = view(true);

  private Cell c2 = view(true);
  private Cell c3 = view(true);
  private Cell c4 = new HorizontalCell();
  private TextCell c41 = navigable();
  private TextCell c42 = navigable();

  @Before
  public void init() {
    CellNavigationController.install(myCellContainer);

    VerticalCell vertical = new VerticalCell();
    vertical.focusable().set(true);
    c1.focusable().set(true);
    c1.children().addAll(Arrays.asList(c11, c12, c13, c14));
    c4.children().addAll(Arrays.asList(c41, c42));
    vertical.children().addAll(Arrays.asList(c1, c2, c3, c4));
    myCellContainer.root.children().add(vertical);

    layout();
  }

  @Test
  public void leftWorks() {
    c13.focus();

    left();

    assertFocused(c11);
  }

  @Test
  public void rightWorks() {
    c13.focus();

    right();

    assertFocused(c14);
  }

  @Test
  public void altRightWorks() {
    c41.focus();
    c41.get(PositionHandler.PROPERTY).end();

    press(Key.RIGHT, ModifierKey.ALT);

    assertFocused(c42);
    assertEquals((Integer) 6, c42.caretPosition().get());
  }

  @Test
  public void altLeftWorks() {
    c42.focus();
    c42.get(PositionHandler.PROPERTY).home();

    press(Key.LEFT, ModifierKey.ALT);

    assertFocused(c41);
    assertEquals((Integer) 0, c41.caretPosition().get());
  }

  @Test
  public void tabWorks() {
    c13.focus();

    tab();

    assertFocused(c14);
  }

  @Test
  public void shiftTabWorks() {
    c13.focus();

    shiftTab();

    assertFocused(c11);
  }

  @Test
  public void simpleUp() {
    c3.focus();

    up();

    assertFocused(c2);
  }

  @Test
  public void simpleDown() {
    c2.focus();

    down();

    assertFocused(c3);
  }

  @Test
  public void upWithManyOptions() {
    c2.focus();

    up();

    assertFocused(c11);
  }

  @Test
  public void upDownDoesntChangeAnything() {
    c13.focus();

    down();
    up();

    assertFocused(c13);
  }

  @Test
  public void homeWorks() {
    c13.focus();

    home();

    assertFocused(c11);
  }

  @Test
  public void endWorks() {
    c11.focus();

    end();

    assertFocused(c14);
  }

  @Test
  public void macHomeWorks() {
    c13.focus();

    macHome();

    assertFocused(c11);
  }

  @Test
  public void macEndWorks() {
    c11.focus();

    macEnd();

    assertFocused(c14);
  }

  @Test
  public void selectionStack() {
    c13.focus();

    altUp();
    altUp();
    altDown();
    altDown();

    assertFocused(c13);
  }

  @Test
  public void selectionStackReset() {
    c13.focus();

    altUp();
    altUp();

    c3.focus();

    altDown();

    assertFocused(c3);
  }

  @Test
  public void stackIsntChangedInCaseWeCantMove() {
    c1.focus();

    altUp();
    altUp();
    altDown();

    assertFocused(c1);
  }

  @Test
  public void clickOnUnownedPlaceEnd() {
    int x = c1.origin().x + c1.dimension().x + 100;
    int y = c1.origin().y + c1.dimension().y / 2;

    c14.addTrait(TextEditing.textNavigation(true, true));

    mousePress(x, y);

    assertFocused(c14);
    assertTrue(c14.get(PositionHandler.PROPERTY).isEnd());
  }

  @Test
  public void clickOnUnownedPlaceHome() {
    int x = -100;
    int y = c1.origin().y + c1.dimension().y / 2;

    mousePress(x, y);

    c11.addTrait(TextEditing.textNavigation(true, true));

    assertFocused(c11);
    assertTrue(c11.get(PositionHandler.PROPERTY).isHome());
  }

  @Test
  public void cantNavigateOutOfPopup() {
    TextCell text = CellFactory.label("");
    HorizontalCell popup = CellFactory.horizontal(text);

    c1.frontPopup().set(popup);
    text.focus();

    right();

    assertTrue(text.focused().get());
  }

  private TextCell view(boolean focusable) {
    TextCell result = new TextCell();
    result.text().set("abcdef");
    result.focusable().set(focusable);
    return result;
  }

  private TextCell navigable() {
    TextCell result = new TextCell();
    result.text().set("abcdef");
    result.addTrait(TextEditing.textEditing());
    return result;
  }
}