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

  public static final KeyStrokeSpec HELP = composite(new KeyStroke(Key.F1, ModifierKey.CONTROL), new KeyStroke(Key.DIGIT_1, ModifierKey.CONTROL));

  public static final KeyStrokeSpec SELECT_ALL = commandOrMeta(Key.A);
  public static final KeyStrokeSpec HOME = composite(new KeyStroke(Key.HOME), new KeyStroke(Key.LEFT, ModifierKey.META));
  public static final KeyStrokeSpec END = composite(new KeyStroke(Key.END), new KeyStroke(Key.RIGHT, ModifierKey.META));

  public static final KeyStrokeSpec FILE_HOME = composite(new KeyStroke(Key.HOME, ModifierKey.CONTROL), new KeyStroke(Key.HOME, ModifierKey.META));
  public static final KeyStrokeSpec FILE_END = composite(new KeyStroke(Key.END, ModifierKey.CONTROL), new KeyStroke(Key.END, ModifierKey.META));

  public static final KeyStroke PREV_WORD_CONTROL = new KeyStroke(Key.LEFT, ModifierKey.CONTROL);
  public static final KeyStroke PREV_WORD_ALT = new KeyStroke(Key.LEFT, ModifierKey.ALT);

  public static final KeyStroke NEXT_WORD_CONTROL = new KeyStroke(Key.RIGHT, ModifierKey.CONTROL);
  public static final KeyStroke NEXT_WORD_ALT = new KeyStroke(Key.RIGHT, ModifierKey.ALT);

  public static final KeyStrokeSpec PREV_WORD = composite(PREV_WORD_CONTROL, PREV_WORD_ALT);
  public static final KeyStrokeSpec NEXT_WORD = composite(NEXT_WORD_CONTROL, NEXT_WORD_ALT);

  public static final KeyStrokeSpec NEXT_EDITABLE = new KeyStroke(Key.TAB);
  public static final KeyStrokeSpec PREV_EDITABLE = new KeyStroke(Key.TAB, ModifierKey.SHIFT);

  public static final KeyStrokeSpec SELECT_HOME = composite(new KeyStroke(Key.HOME, ModifierKey.SHIFT), new KeyStroke(Key.LEFT, ModifierKey.META, ModifierKey.SHIFT));
  public static final KeyStrokeSpec SELECT_END = composite(new KeyStroke(Key.END, ModifierKey.SHIFT), new KeyStroke(Key.RIGHT, ModifierKey.META, ModifierKey.SHIFT));

  public static final KeyStrokeSpec SELECT_UP = new KeyStroke(Key.UP, ModifierKey.ALT);
  public static final KeyStrokeSpec SELECT_DOWN = new KeyStroke(Key.DOWN, ModifierKey.ALT);

  public static final KeyStrokeSpec SELECT_BEFORE = composite(new KeyStroke(Key.UP, ModifierKey.SHIFT), new KeyStroke(Key.LEFT, ModifierKey.SHIFT));
  public static final KeyStrokeSpec SELECT_AFTER = composite(new KeyStroke(Key.DOWN, ModifierKey.SHIFT), new KeyStroke(Key.RIGHT, ModifierKey.SHIFT));

  public static final KeyStrokeSpec INSERT_BEFORE = composite(new KeyStroke(Key.INSERT), new KeyStroke(Key.ENTER, ModifierKey.SHIFT));
  public static final KeyStrokeSpec INSERT_AFTER = new KeyStroke(Key.ENTER);

  public static final KeyStrokeSpec INSERT = composite(INSERT_BEFORE, INSERT_AFTER);

  public static final KeyStrokeSpec DELETE_CURRENT = composite(new KeyStroke(Key.BACKSPACE, ModifierKey.META), new KeyStroke(Key.DELETE, ModifierKey.META), new KeyStroke(Key.DELETE, ModifierKey.CONTROL), new KeyStroke(Key.BACKSPACE, ModifierKey.CONTROL));

  public static final KeyStrokeSpec MATCHING_CONSTRUCTS = composite(commandOrMeta(Key.LEFT_BRACE), commandOrMeta(Key.RIGHT_BRACE));

  public static final KeyStrokeSpec NAVIGATE = commandOrMeta(Key.B);

  public static KeyStrokeSpec commandOrMeta(Key key, ModifierKey... modifiers) {
    return composite(new KeyStroke(key, add(ModifierKey.CONTROL, modifiers)), new KeyStroke(key, add(ModifierKey.META, modifiers)));
  }

  private static Set<ModifierKey> add(ModifierKey key, ModifierKey... otherKeys) {
    Set<ModifierKey> result = new HashSet<>(Arrays.asList(otherKeys));
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
      public Set<KeyStroke> getKeyStrokes() {
        Set<KeyStroke> result = new LinkedHashSet<>();
        for (KeyStrokeSpec s : specs) {
          result.addAll(s.getKeyStrokes());
        }
        return result;
      }
    };
  }
}