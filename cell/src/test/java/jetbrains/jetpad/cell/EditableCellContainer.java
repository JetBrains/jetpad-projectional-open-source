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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.ViewContainer;

import java.util.Collections;

public class EditableCellContainer {
  public final CellContainer container;

  private ViewContainer myViewContainer = new ViewContainer();

  public EditableCellContainer(CellContainer container) {
    this.container = container;
    CellToView.map(container, myViewContainer);
  }

  public void layout() {
    myViewContainer.root().validate();
  }

  public void type(String s) {
    for (int i = 0; i < s.length(); i++) {
      type(s.charAt(i));
    }
  }

  public KeyEvent type(char c) {
    KeyEvent event = new KeyEvent(null, c, Collections.<ModifierKey>emptySet());
    myViewContainer.keyTyped(event);
    return event;
  }

  public KeyEvent press(Key key, ModifierKey... modifiers) {
    return press(new KeyStroke(key, modifiers));
  }

  public KeyEvent press(KeyStrokeSpec spec) {
    if (spec.getKeyStrokes().isEmpty()) {
      throw new IllegalArgumentException();
    }
    return press(spec.getKeyStrokes().iterator().next());
  }

  public KeyEvent press(KeyStroke ks) {
    KeyEvent event = new KeyEvent(ks.getKey(), (char) 0, ks.getModifiers());
    myViewContainer.keyPressed(event);
    return event;
  }

  public void paste(String text) {
    container.paste(text);
  }

  public final void complete() {
    container.complete(new CompletionEvent(false));
  }

  public final void enter() {
    press(Key.ENTER);
  }

  public final void shiftEnter() {
    press(Key.ENTER, ModifierKey.SHIFT);
  }

  public final void escape() {
    press(Key.ESCAPE);
  }

  public final void up() {
    press(Key.UP);
  }

  public final void altUp() {
    press(Key.UP, ModifierKey.ALT);
  }

  public final void down() {
    press(Key.DOWN);
  }

  public final void altDown() {
    press(Key.DOWN, ModifierKey.ALT);
  }

  public final void left() {
    press(Key.LEFT);
  }

  public final void right() {
    press(Key.RIGHT);
  }

  public final void altLeft() {
    press(Key.LEFT, ModifierKey.ALT);
  }

  public final void altRight() {
    press(Key.RIGHT, ModifierKey.ALT);
  }

  public final void home() {
    press(Key.HOME);
  }

  public final void macHome() {
    press(Key.LEFT, ModifierKey.META);
  }

  public final void end() {
    press(Key.END);
  }

  public final void macEnd() {
    press(Key.RIGHT, ModifierKey.META);
  }

  public final void tab() {
    press(Key.TAB);
  }

  public final void shiftTab() {
    press(Key.TAB, ModifierKey.SHIFT);
  }

  public final void backspace() {
    press(Key.BACKSPACE);
  }

  public final void del() {
    press(Key.DELETE);
  }

  public final void insert() {
    press(Key.INSERT);
  }

  public final void mousePress(int x, int y) {
    myViewContainer.mousePressed(new MouseEvent(x, y));
  }

  public final void mouseDrag(int x, int y) {
    myViewContainer.mouseDragged(new MouseEvent(x, y));
  }

  public final void mousePress(Vector v) {
    mousePress(v.x, v.y);
  }

  public final void mouseDrag(Vector v) {
    mouseDrag(v.x, v.y);
  }
}
