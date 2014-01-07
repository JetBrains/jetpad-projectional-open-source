package jetbrains.jetpad.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeyStrokeSpecs {
  public static final KeyStrokeSpec COPY = commandOrMeta(Key.C);
  public static final KeyStrokeSpec CUT = commandOrMeta(Key.X);
  public static final KeyStrokeSpec PASTE = commandOrMeta(Key.V);

  public static final KeyStrokeSpec UNDO = commandOrMeta(Key.Z);
  public static final KeyStrokeSpec REDO = commandOrMeta(Key.Z, ModifierKey.SHIFT);

  public static final KeyStrokeSpec COMPLETE = composite(new KeyStroke(Key.SPACE, ModifierKey.CONTROL));

  public static final KeyStrokeSpec SELECT_ALL = commandOrMeta(Key.A);
  public static final KeyStrokeSpec HOME = composite(new KeyStroke(Key.HOME), new KeyStroke(Key.LEFT, ModifierKey.META));
  public static final KeyStrokeSpec END = composite(new KeyStroke(Key.END), new KeyStroke(Key.RIGHT, ModifierKey.META));


  public static KeyStrokeSpec commandOrMeta(Key key, ModifierKey... modifiers) {
    return composite(new KeyStroke(key, add(ModifierKey.CONTROL, modifiers)), new KeyStroke(key, add(ModifierKey.META, modifiers)));
  }

  private static Set<ModifierKey> add(ModifierKey key, ModifierKey... otherKeys) {
    Set<ModifierKey> result = new HashSet<ModifierKey>(Arrays.asList(otherKeys));
    result.add(key);
    return result;
  }

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
