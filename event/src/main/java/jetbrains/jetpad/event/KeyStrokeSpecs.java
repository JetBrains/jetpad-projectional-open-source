package jetbrains.jetpad.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

  public static final KeyStrokeSpec SELECT_HOME = composite(new KeyStroke(Key.HOME, ModifierKey.SHIFT), new KeyStroke(Key.LEFT, ModifierKey.META, ModifierKey.SHIFT));
  public static final KeyStrokeSpec SELECT_END = composite(new KeyStroke(Key.END, ModifierKey.SHIFT), new KeyStroke(Key.RIGHT, ModifierKey.META, ModifierKey.SHIFT));

  public static final KeyStrokeSpec SELECT_UP = new KeyStroke(Key.UP, ModifierKey.ALT);
  public static final KeyStrokeSpec SELECT_DOWN = new KeyStroke(Key.DOWN, ModifierKey.ALT);

  public static final KeyStrokeSpec SELECT_BEFORE = composite(new KeyStroke(Key.UP, ModifierKey.SHIFT), new KeyStroke(Key.LEFT, ModifierKey.SHIFT));
  public static final KeyStrokeSpec SELECT_AFTER = composite(new KeyStroke(Key.DOWN, ModifierKey.SHIFT), new KeyStroke(Key.RIGHT, ModifierKey.SHIFT));

  public static final KeyStrokeSpec INSERT_BEFORE = composite(new KeyStroke(Key.INSERT), new KeyStroke(Key.ENTER, ModifierKey.SHIFT));
  public static final KeyStrokeSpec INSERT_AFTER = new KeyStroke(Key.ENTER);

  public static final KeyStrokeSpec DELETE_CURRENT = composite(new KeyStroke(Key.BACKSPACE, ModifierKey.META), new KeyStroke(Key.DELETE, ModifierKey.META), new KeyStroke(Key.DELETE, ModifierKey.CONTROL), new KeyStroke(Key.BACKSPACE, ModifierKey.CONTROL));


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
        Set<KeyStroke> result = new LinkedHashSet<KeyStroke>();
        for (KeyStrokeSpec s : specs) {
          result.addAll(s.keyStrokes());
        }
        return result;
      }
    };
  }
}