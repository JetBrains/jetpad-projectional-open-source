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
package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.event.FocusEvent;

public interface CellTrait {
  public static final Object NULL = new Object();
  public static final CellTrait[] EMPTY_ARRAY = new CellTrait[0];

  void onPropertyChanged(Cell cell, CellPropertySpec<?> property, PropertyChangeEvent<?> event);

  void onFocusGained(Cell cell, FocusEvent event);
  void onFocusLost(Cell cell, FocusEvent event);

  void onMousePressed(Cell cell, MouseEvent event);
  void onMouseMoved(Cell cell, MouseEvent event);
  void onMouseReleased(Cell cell, MouseEvent event);
  void onMouseEntered(Cell cell, MouseEvent event);
  void onMouseLeft(Cell cell, MouseEvent event);

  void onKeyPressed(Cell cell, KeyEvent event);
  void onKeyPressedLowPriority(Cell cell, KeyEvent event);

  void onKeyReleased(Cell cell, KeyEvent event);
  void onKeyReleasedLowPriority(Cell cell, KeyEvent event);

  void onKeyTyped(Cell cell, KeyEvent event);
  void onKeyTypedLowPriority(Cell cell, KeyEvent event);

  void onCopy(Cell cell, CopyCutEvent event);
  void onCut(Cell cell, CopyCutEvent event);
  void onPaste(Cell cell, PasteEvent event);

  void onComplete(Cell cell, CompletionEvent event);

  void onViewTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event);

  Object get(Cell cell, CellPropertySpec<?> spec);
  Object get(Cell cell, CellTraitPropertySpec<?> spec);
}