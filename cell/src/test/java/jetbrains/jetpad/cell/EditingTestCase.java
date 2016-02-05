/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.test.BaseTestCase;

import static org.junit.Assert.*;

public abstract class EditingTestCase extends BaseTestCase {
  protected final CellContainer myCellContainer = new CellContainer();

  private EditableCellContainer myEditableCellContainer = new EditableCellContainer(myCellContainer);

  protected void layout() {
    myEditableCellContainer.layout();
  }

  protected void type(String s) {
    myEditableCellContainer.type(s);
  }

  protected KeyEvent type(char c) {
    return myEditableCellContainer.type(c);
  }

  protected KeyEvent press(Key key, ModifierKey... modifiers) {
    return myEditableCellContainer.press(key, modifiers);
  }

  protected KeyEvent press(KeyStrokeSpec spec) {
    return myEditableCellContainer.press(spec);
  }

  protected KeyEvent press(KeyStroke ks) {
    return myEditableCellContainer.press(ks);
  }

  protected void paste(String text) {
    myEditableCellContainer.paste(text);
  }

  protected final void complete() {
    myEditableCellContainer.complete();
  }

  protected final void enter() {
    myEditableCellContainer.enter();
  }

  protected final void shiftEnter() {
    myEditableCellContainer.shiftEnter();
  }

  protected final void escape() {
    myEditableCellContainer.escape();
  }

  protected final void help() {
    myEditableCellContainer.help();
  }

  protected final void up() {
    myEditableCellContainer.up();
  }

  protected final void altUp() {
    myEditableCellContainer.altUp();
  }

  protected final void down() {
    myEditableCellContainer.down();
  }

  protected final void altDown() {
    myEditableCellContainer.altDown();
  }

  protected final void left() {
    myEditableCellContainer.left();
  }

  protected final void right() {
    myEditableCellContainer.right();
  }

  protected final void altLeft() {
    myEditableCellContainer.altLeft();
  }

  protected final void altRight() {
    myEditableCellContainer.altRight();
  }

  protected final void home() {
    myEditableCellContainer.home();
  }

  protected final void macHome() {
    myEditableCellContainer.macHome();
  }

  protected final void end() {
    myEditableCellContainer.end();
  }

  protected final void macEnd() {
    myEditableCellContainer.macEnd();
  }

  protected final void backspace() {
    myEditableCellContainer.backspace();
  }

  protected final void del() {
    myEditableCellContainer.del();
  }

  protected final void insert() {
    myEditableCellContainer.insert();
  }

  protected final void mousePress(int x, int y) {
    myEditableCellContainer.mousePress(x, y);
  }

  protected final void mouseDrag(int x, int y) {
    myEditableCellContainer.mouseDrag(x, y);
  }

  protected final void mousePress(Vector v) {
    myEditableCellContainer.mousePress(v);
  }

  protected final void mouseDrag(Vector v) {
    myEditableCellContainer.mouseDrag(v);
  }

  protected void assertFocused(Cell cell) {
    assertSame(cell, myCellContainer.focusedCell.get());
  }

  protected void assertNotFocused(Cell cell) {
    assertNotSame(cell, myCellContainer.focusedCell.get());
  }

  protected void assertHasBottomPopup(Cell cell) {
    assertNotNull(cell.bottomPopup().get());
  }

  protected void assertNoBottomPopup(Cell cell) {
    assertNull(cell.bottomPopup().get());
  }

  protected void assertHasFrontPopup(Cell cell) {
    assertNotNull(cell.frontPopup().get());
  }

  protected void assertNoFrontPopup(Cell cell) {
    assertNull(cell.frontPopup().get());
  }

  protected void assertConsumed(Event... es) {
    for (Event e : es) {
      assertTrue(e.isConsumed());
    }
  }
}