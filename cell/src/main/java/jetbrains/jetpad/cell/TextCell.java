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

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.values.Color;

public class TextCell extends Cell {
  public static final CellPropertySpec<String> TEXT = new CellPropertySpec<>("text", "");
  public static final CellPropertySpec<Color> TEXT_COLOR = new CellPropertySpec<>("textColor", Color.BLACK);
  public static final CellPropertySpec<Boolean> CARET_VISIBLE = new CellPropertySpec<>("caretVisible", false);
  public static final CellPropertySpec<Integer> CARET_POSITION = new CellPropertySpec<>("caretPosition", 0);
  public static final CellPropertySpec<Boolean> BOLD = new CellPropertySpec<>("bold", false);

  public static final CellPropertySpec<Boolean> SELECTION_VISIBLE = new CellPropertySpec<>("selectioNVisible", false);
  public static final CellPropertySpec<Integer> SELECTION_START = new CellPropertySpec<>("selectionStart", 0);

  public TextCell() {
  }

  public TextCell(String text) {
    text().set(text);
  }

  public Property<String> text() {
    return getProp(TEXT);
  }

  public Property<Color> textColor() {
    return getProp(TEXT_COLOR);
  }

  public Property<Boolean> caretVisible() {
    return getProp(CARET_VISIBLE);
  }

  public Property<Boolean> bold() {
    return getProp(BOLD);
  }

  public Property<Integer> caretPosition() {
    return getProp(CARET_POSITION);
  }

  public Property<Boolean> selectionVisible() {
    return getProp(SELECTION_VISIBLE);
  }

  public Property<Integer> selectionStart() {
    return getProp(SELECTION_START);
  }

  public boolean isEnd() {
    return caretPosition().get() == getLastPosition();
  }

  public boolean isHome() {
    return caretPosition().get() == 0;
  }

  private int getLastPosition() {
    return text().get() == null ? 0 : text().get().length();
  }

  public int getCaretAt(int x) {
    return getViewContainerPeer().getCaretAt(this, x);
  }

  public int getCaretOffset(int caret) {
    return getViewContainerPeer().getCaretOffset(this, caret);
  }

  public void scrollToCaret() {
    int delta = 50;
    int offset = getCaretOffset(caretPosition().get());
    Rectangle bounds = getBounds();
    scrollTo(new Rectangle(offset - delta, 0, 2 * delta, bounds.dimension.y).intersect(new Rectangle(Vector.ZERO, bounds.dimension)));
  }

  public ReadableProperty<String> prefixText() {
    return new DerivedProperty<String>(text(), caretPosition()) {
      @Override
      public String get() {
        int caret = caretPosition().get();
        String textValue = text().get() != null ? text().get() : "";
        return textValue.substring(0, caret);
      }

      @Override
      public String getPropExpr() {
        return "prefixText(" + TextCell.this + ")";
      }
    };
  }

  @Override
  public String toString() {
    return "TextCell('" + text().get() + "')";
  }
}