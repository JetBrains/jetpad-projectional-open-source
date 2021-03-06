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
package jetbrains.jetpad.event.awt;

import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.event.MouseEvent;

import java.util.HashSet;
import java.util.Set;

public class EventTranslator {
  public static MouseEvent translate(java.awt.event.MouseEvent e) {
    return new MouseEvent(e.getX(), e.getY());
  }

  public static KeyEvent translate(java.awt.event.KeyEvent e) {
    Set<ModifierKey> modifiers = new HashSet<>();
    if (e.isControlDown()) {
      modifiers.add(ModifierKey.CONTROL);
    }
    if (e.isAltDown()) {
      modifiers.add(ModifierKey.ALT);
    }
    if (e.isShiftDown()) {
      modifiers.add(ModifierKey.SHIFT);
    }
    if (e.isMetaDown()) {
      modifiers.add(ModifierKey.META);
    }
    return new KeyEvent(KeyCodeMapper.keyByKeyCode(e.getKeyCode()), e.getKeyChar(), modifiers);
  }
}