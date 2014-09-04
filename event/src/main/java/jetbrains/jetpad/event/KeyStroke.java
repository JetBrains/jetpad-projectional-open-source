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

import com.google.common.base.Objects;

import java.util.*;

public class KeyStroke implements KeyStrokeSpec {
  private Key myKey;
  private Set<ModifierKey> myModifiers;

  public KeyStroke(Key key, ModifierKey... modifiers) {
    this(key, Arrays.asList(modifiers));
  }

  public KeyStroke(Key key, Collection<ModifierKey> modifiers) {
    myKey = key;
    myModifiers = new HashSet<>(modifiers);
  }

  public Key key() {
    return myKey;
  }

  public Set<ModifierKey> modifiers() {
    return Collections.unmodifiableSet(myModifiers);
  }

  public boolean has(ModifierKey key) {
    return myModifiers.contains(key);
  }

  public boolean is(Key key, ModifierKey... modifiers) {
    return matches(new KeyStroke(key, modifiers));
  }

  @Override
  public boolean matches(KeyStroke keyStroke) {
    return equals(keyStroke);
  }

  @Override
  public Set<KeyStroke> keyStrokes() {
    return Collections.singleton(this);
  }

  public KeyStroke with(ModifierKey key) {
    Set<ModifierKey> keys = new HashSet<>(myModifiers);
    keys.add(key);
    return new KeyStroke(myKey, keys);
  }

  @Override
  public int hashCode() {
    return myKey.hashCode() * 31 + myModifiers.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof KeyStroke)) return false;
    KeyStroke otherKeyStroke = (KeyStroke) obj;

    return myKey == otherKeyStroke.myKey &&
      Objects.equal(myModifiers, otherKeyStroke.myModifiers);
  }

  @Override
  public String toString() {
    return myKey + " " + myModifiers;
  }
}