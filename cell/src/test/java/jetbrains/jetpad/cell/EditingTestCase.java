/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;

import java.util.Collections;

import static org.junit.Assert.*;

public abstract class EditingTestCase {
  protected final CellContainer myCellContainer = new CellContainer();

  private ViewContainer myViewContainer = new ViewContainer();

  @Before
  public void initLViewContainer() {
    CellToView.map(myCellContainer, myViewContainer);
  }

  protected void layout() {
    myViewContainer.root().validate();
  }

  protected void type(String s) {
    for (int i = 0; i < s.length(); i++) {
      type(s.charAt(i));
    }
  }

  protected KeyEvent type(char c) {
    KeyEvent event = new KeyEvent(null, c, Collections.<ModifierKey>emptySet());
    myViewContainer.keyTyped(event);
    return event;
  }

  protected KeyEvent press(Key key, ModifierKey... modifiers) {
    return press(new KeyStroke(key, modifiers));
  }

  protected KeyEvent press(KeyStrokeSpec spec) {
    if (spec.getKeyStrokes().isEmpty()) {
      throw new IllegalArgumentException();
    }
    return press(spec.getKeyStrokes().iterator().next());
  }

  protected KeyEvent press(KeyStroke ks) {
    KeyEvent event = new KeyEvent(ks.getKey(), (char) 0, ks.getModifiers());
    myViewContainer.keyPressed(event);
    return event;
  }

  protected void paste(String text) {
    myCellContainer.paste(text);
  }

  protected final void complete() {
    myCellContainer.complete(new CompletionEvent(false));
  }

  protected final void enter() {
    press(Key.ENTER);
  }

  protected final void shiftEnter() {
    press(Key.ENTER, ModifierKey.SHIFT);
  }

  protected final void escape() {
    press(Key.ESCAPE);
  }

  protected final void up() {
    press(Key.UP);
  }

  protected final void altUp() {
    press(Key.UP, ModifierKey.ALT);
  }

  protected final void down() {
    press(Key.DOWN);
  }

  protected final void altDown() {
    press(Key.DOWN, ModifierKey.ALT);
  }

  protected final void left() {
    press(Key.LEFT);
  }

  protected final void right() {
    press(Key.RIGHT);
  }

  protected final void altLeft() {
    press(Key.LEFT, ModifierKey.ALT);
  }

  protected final void altRight() {
    press(Key.RIGHT, ModifierKey.ALT);
  }

  protected final void home() {
    press(Key.HOME);
  }

  protected final void macHome() {
    press(Key.LEFT, ModifierKey.META);
  }

  protected final void end() {
    press(Key.END);
  }

  protected final void macEnd() {
    press(Key.RIGHT, ModifierKey.META);
  }

  protected final void tab() {
    press(Key.TAB);
  }

  protected final void shiftTab() {
    press(Key.TAB, ModifierKey.SHIFT);
  }

  protected final void backspace() {
    press(Key.BACKSPACE);
  }

  protected final void del() {
    press(Key.DELETE);
  }

  protected final void insert() {
    press(Key.INSERT);
  }

  protected final void mousePress(int x, int y) {
    myViewContainer.mousePressed(new MouseEvent(x, y));
  }

  protected final void mouseDrag(int x, int y) {
    myViewContainer.mouseDragged(new MouseEvent(x, y));
  }

  protected final void mousePress(Vector v) {
    mousePress(v.x, v.y);
  }

  protected final void mouseDrag(Vector v) {
    mouseDrag(v.x, v.y);
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
}