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
package jetbrains.jetpad.event;

import java.util.*;

public class KeyEvent extends Event {
  private Key myKey;
  private Set<ModifierKey> myModifiers;
  private char myKeyChar;

  public KeyEvent(Key key) {
    this(key, (char) 0, Collections.<ModifierKey>emptyList());
  }

  public KeyEvent(Key key, char ch, Collection<ModifierKey> modifiers) {
    myKey = key;
    myModifiers = new HashSet<ModifierKey>(modifiers);
    myKeyChar = ch;
  }

  public Key key() {
    return myKey;
  }

  public char keyChar() {
    return myKeyChar;
  }

  public Set<ModifierKey> modifiers() {
    return Collections.unmodifiableSet(myModifiers);
  }

  public boolean is(Key key, ModifierKey... modifiers) {
    if (key != myKey) return false;
    return myModifiers.equals(new HashSet<ModifierKey>(Arrays.asList(modifiers)));
  }

  public boolean has(ModifierKey key) {
    return myModifiers.contains(key);
  }

  @Override
  public String toString() {
    return myKey + " " + myModifiers;
  }
}