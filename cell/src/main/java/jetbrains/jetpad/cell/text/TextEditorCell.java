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
package jetbrains.jetpad.cell.text;

import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

public interface TextEditorCell {
  public static final CellPropertySpec<String> TEXT = new CellPropertySpec<>("text", "");
  public static final CellPropertySpec<Color> TEXT_COLOR = new CellPropertySpec<>("textColor", Color.BLACK);

  Property<String> text();
  Property<Color> textColor();

  Property<Boolean> selectionVisible();
  Property<Integer> selectionStart();

  Property<Boolean> caretVisible();
  Property<Integer> caretPosition();
  void scrollToCaret();
  int getCaretAt(int x);
  int getCaretOffset(int caret);
}
