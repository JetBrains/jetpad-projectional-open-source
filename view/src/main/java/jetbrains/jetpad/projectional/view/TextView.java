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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;

public class TextView extends View {
  public static final Font DEFAULT_FONT = new Font(FontFamily.MONOSPACED, 15);

  public static final ViewPropertySpec<String> TEXT = new ViewPropertySpec<>("text", ViewPropertyKind.RELAYOUT, "");
  public static final ViewPropertySpec<Color> TEXT_COLOR = new ViewPropertySpec<>("color", ViewPropertyKind.REPAINT, Color.BLACK);

  public static final ViewPropertySpec<Boolean> BOLD = new ViewPropertySpec<>("bold", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Boolean> ITALIC = new ViewPropertySpec<>("italic", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<FontFamily> FONT_FAMILY = new ViewPropertySpec<>("fontFamily", ViewPropertyKind.RELAYOUT_AND_REPAINT, DEFAULT_FONT.getFamily());
  public static final ViewPropertySpec<Integer> FONT_SIZE = new ViewPropertySpec<>("fontSize", ViewPropertyKind.RELAYOUT_AND_REPAINT, DEFAULT_FONT.getSize());

  public static final ViewPropertySpec<Boolean> CARET_VISIBLE = new ViewPropertySpec<>("caretVisible", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Integer> CARET_POSITION = new ViewPropertySpec<>("caretPositiong", ViewPropertyKind.REPAINT, 0);

  public static final ViewPropertySpec<Boolean> SELECTION_VISIBLE = new ViewPropertySpec<>("selectionVisible", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Integer> SELECTION_START = new ViewPropertySpec<>("selectionStart", ViewPropertyKind.REPAINT, 0);

  public TextView() {
  }

  public TextView(String text) {
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

  public Property<Integer> caretPosition() {
    return getProp(CARET_POSITION);
  }

  public Property<Boolean> bold() {
    return getProp(BOLD);
  }

  public Property<Boolean> italic() {
    return getProp(ITALIC);
  }

  public Property<FontFamily> fontFamily() {
    return getProp(FONT_FAMILY);
  }

  public Property<Integer> fontSize() {
    return getProp(FONT_SIZE);
  }

  public ReadableProperty<Font> font() {
    return new DerivedProperty<Font>(fontFamily(), fontSize(), bold(), italic()) {
      @Override
      public Font doGet() {
        return new Font(fontFamily().get(), fontSize().get(), bold().get(), italic().get());
      }
    };
  }

  public Property<Boolean> selectionVisible() {
    return getProp(SELECTION_VISIBLE);
  }

  public Property<Integer> selectionStart() {
    return getProp(SELECTION_START);
  }

  public int getCaretAt(int xOffset) {

    String text = this.text().get();
    if (text == null) return 0;
    for (int i = 0; i < text.length(); i++) {
      int width = (textWidth(text.substring(0, i)) + textWidth(text.substring(0, i + 1))) / 2;
      if (width >= xOffset) return i;
    }
    return text.length();
  }


  public int getCaretOffset(int caret) {
    if (container() == null) {
      throw new IllegalStateException();
    }
    if (text().get() == null) return 0;
    return textWidth(text().get().substring(0, caret));
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    String text = text().get();
    Vector bounds = new Vector(textWidth(text) + 1, textHeight());
    ctx.bounds(new Vector(bounds.x, bounds.y), textBaseLine());
  }

  private int textWidth(String text) {
    ViewContainerPeer peer = container().peer();
    return peer.textWidth(font().get(), text);
  }

  private int textHeight() {
    ViewContainerPeer peer = container().peer();
    return peer.textHeight(font().get());
  }

  private int textBaseLine() {
    ViewContainerPeer peer = container().peer();
    return peer.textBaseLine(font().get());
  }

  @Override
  protected String toStringPrefix() {
    return "TextView('" + text().get() + "')";
  }
}