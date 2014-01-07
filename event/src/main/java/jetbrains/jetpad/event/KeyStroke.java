package jetbrains.jetpad.event;

import com.google.common.base.Objects;

import java.util.*;

public class KeyStroke {
  private Key myKey;
  private Set<ModifierKey> myModifiers;

  public KeyStroke(Key key, ModifierKey... modifiers) {
    this(key, Arrays.asList(modifiers));
  }

  public KeyStroke(Key key, Collection<ModifierKey> modifiers) {
    myKey = key;
    myModifiers = new HashSet<ModifierKey>(modifiers);
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
    return equals(new KeyStroke(key, modifiers));
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
