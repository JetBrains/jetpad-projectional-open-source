package jetbrains.jetpad.event;

import java.util.HashSet;
import java.util.Set;

public class KeyStrokeSpecs {
  public static final KeyStrokeSpec COPY = composite(new KeyStroke(Key.C, ModifierKey.CONTROL), new KeyStroke(Key.C, ModifierKey.META));
  public static final KeyStrokeSpec CUT = composite(new KeyStroke(Key.X, ModifierKey.CONTROL), new KeyStroke(Key.X, ModifierKey.META));
  public static final KeyStrokeSpec PASTE = composite(new KeyStroke(Key.V, ModifierKey.CONTROL), new KeyStroke(Key.V, ModifierKey.META));

  public static final KeyStrokeSpec COMPLETE = composite(new KeyStroke(Key.SPACE, ModifierKey.CONTROL));

  public static final KeyStrokeSpec UNDO = composite(new KeyStroke(Key.Z, ModifierKey.CONTROL), new KeyStroke(Key.Z, ModifierKey.META));
  public static final KeyStrokeSpec REDO = composite(new KeyStroke(Key.Z, ModifierKey.CONTROL, ModifierKey.SHIFT), new KeyStroke(Key.Z, ModifierKey.META, ModifierKey.SHIFT));

  public static KeyStrokeSpec composite(final KeyStrokeSpec... specs) {
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
