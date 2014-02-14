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
package jetbrains.jetpad.event;

import java.util.*;

public class KeyEvent extends Event {
  private KeyStroke myKeyStroke;
  private char myKeyChar;

  public KeyEvent(Key key) {
    this(key, (char) 0, Collections.<ModifierKey>emptyList());
  }

  public KeyEvent(Key key, char ch, Collection<ModifierKey> modifiers) {
    myKeyStroke = new KeyStroke(key, modifiers);
    myKeyChar = ch;
  }

  public Key key() {
    return myKeyStroke.key();
  }

  public KeyStroke keyStroke() {
    return myKeyStroke;
  }

  public char keyChar() {
    return myKeyChar;
  }

  public Set<ModifierKey> modifiers() {
    return myKeyStroke.modifiers();
  }

  public boolean is(Key key, ModifierKey... modifiers) {
    return myKeyStroke.is(key, modifiers);
  }

  public boolean is(KeyStrokeSpec spec) {
    return spec.matches(myKeyStroke);
  }

  public boolean has(ModifierKey key) {
    return myKeyStroke.has(key);
  }

  @Override
  public String toString() {
    return myKeyStroke.toString();
  }

  @Override
  public void consume() {
    super.consume();
  }
}