package jetbrains.jetpad.event;

import java.util.HashSet;
import java.util.Set;

public class KeyStrokeSpecs {
  public KeyStrokeSpec composite(final KeyStrokeSpec... specs) {
    return new KeyStrokeSpec() {
      @Override
      public boolean matches(KeyStroke keyStroke) {
        for (KeyStrokeSpec s : specs) {
          if (s.matches(keyStroke)) return true;
        }
        return false;
      }

      @Override
      public Set<KeyStroke> keyStrokes() {
        Set<KeyStroke> result = new HashSet<KeyStroke>();
        for (KeyStrokeSpec s : specs) {
          result.addAll(s.keyStrokes());
        }
        return result;
      }
    };
  }
}
