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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Color;

public class TextView extends View {
  public static final ViewPropertySpec<String> TEXT = new ViewPropertySpec<String>("text", ViewPropertyKind.RELAYOUT, "");
  public static final ViewPropertySpec<Color> TEXT_COLOR = new ViewPropertySpec<Color>("color", ViewPropertyKind.REPAINT, Color.BLACK);
  public static final ViewPropertySpec<Boolean> CARET_VISIBLE = new ViewPropertySpec<Boolean>("caretVisible", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Integer> CARET_POSITION = new ViewPropertySpec<Integer>("caretPositiong", ViewPropertyKind.REPAINT, 0);
  public static final ViewPropertySpec<Boolean> BOLD = new ViewPropertySpec<Boolean>("bold", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Boolean> SELECTION_VISIBLE = new ViewPropertySpec<Boolean>("selectionVisible", ViewPropertyKind.REPAINT, false);
  public static final ViewPropertySpec<Integer> SELECTION_START = new ViewPropertySpec<Integer>("selectionStart", ViewPropertyKind.REPAINT, 0);

  public TextView() {
  }

  public TextView(String text) {
    text().set(text);
  }

  public Property<String> text() {
    return prop(TEXT);
  }

  public Property<Color> textColor() {
    return prop(TEXT_COLOR);
  }

  public Property<Boolean> caretVisible() {
    return prop(CARET_VISIBLE);
  }

  public Property<Integer> caretPosition() {
    return prop(CARET_POSITION);
  }

  public Property<Boolean> bold() {
    return prop(BOLD);
  }

  public Property<Boolean> selectionVisible() {
    return prop(SELECTION_VISIBLE);
  }

  public Property<Integer> selectionStart() {
    return prop(SELECTION_START);
  }

  public int getCaretAt(int xOffset) {
    ViewContainerPeer peer = container().peer();
    String text = this.text().get();
    if (text == null) return 0;
    for (int i = 0; i < text.length(); i++) {
      int width = (peer.textWidth(text.substring(0, i)) + peer.textWidth(text.substring(0, i + 1))) / 2;
      if (width >= xOffset) return i;
    }
    return text.length();
  }

  public int getCaretOffset(int caret) {
    if (container() == null) throw new IllegalStateException();
    if (text().get() == null) return 0;
    ViewContainerPeer peer = container().peer();
    return peer.textWidth(text().get().substring(0, caret));
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    ViewContainerPeer peer = container().peer();
    String text = text().get();
    Vector bounds = new Vector(peer.textWidth(text) + 1, peer.textHeight());
    ctx.bounds(new Vector(bounds.x, bounds.y), peer.textBaseLine());
  }

  @Override
  protected String toStringPrefix() {
    return "TextView('" + text().get() + "')";
  }
}