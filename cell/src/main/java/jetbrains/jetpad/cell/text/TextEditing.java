/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.cell.text;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.completion.Side;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateDifference;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.values.Color;

public class TextEditing {
  public static final CellTraitPropertySpec<Boolean> FIRST_ALLOWED = new CellTraitPropertySpec<>("firstAllowed", true);
  public static final CellTraitPropertySpec<Boolean> LAST_ALLOWED = new CellTraitPropertySpec<>("firstAllowed", true);
  public static final CellTraitPropertySpec<Boolean> RT_ON_END = new CellTraitPropertySpec<>("dotLikeRt", false);
  public static final CellTraitPropertySpec<Boolean> CLEAR_ON_DELETE = new CellTraitPropertySpec<>("clearOnDelete", false);

  public static final CellTraitPropertySpec<Function<String, Runnable>> EXPAND_LEFT = new CellTraitPropertySpec<>("expandLeft", expansionProvider(Side.LEFT));
  public static final CellTraitPropertySpec<Function<String, Runnable>> EXPAND_RIGHT = new CellTraitPropertySpec<>("expandRight", expansionProvider(Side.RIGHT));
  public static final CellTraitPropertySpec<Supplier<Boolean>> AFTER_TYPE = new CellTraitPropertySpec<>("afterType");

  public static final CellTraitPropertySpec<Boolean> EAGER_COMPLETION = new CellTraitPropertySpec<>("eagerCompletion", false);

  private static final TextEditorStateHandler STATE_HANDLER = new TextEditorStateHandler(false, null);


  public static boolean isTextEditor(Cell cell) {
    return cell instanceof TextCell;
  }

  public static CellTextEditor textEditor(Cell cell) {
    if (cell instanceof TextCell) {
      return new TextCellToEditorAdapter((TextCell) cell);
    }
    throw new RuntimeException("Cannot create text editor for " + cell);
  }

  public static boolean isHome(TextEditor t) {
    return t.caretPosition().get() == 0;
  }

  public static boolean isEnd(TextEditor t) {
    return t.caretPosition().get() == lastPosition(t);
  }

  public static boolean isEmpty(TextEditor t) {
    return lastPosition(t) == 0;
  }

  static int lastPosition(TextEditor t) {
    return text(t).length();
  }

  public static String text(TextEditor t) {
    String text = t.text().get();
    return text == null ? "" : text;
  }

  public static String getPrefixText(TextEditor t) {
    return text(t).substring(0, t.caretPosition().get());
  }

  public static CellTrait textNavigation(final boolean firstAllowed, final boolean lastAllowed) {
    return new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return new TextNavigationTrait();
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == FIRST_ALLOWED) {
          return firstAllowed;
        }

        if (spec == LAST_ALLOWED) {
          return lastAllowed;
        }

        if (spec == CellStateHandler.PROPERTY) {
          return STATE_HANDLER;
        }

        return super.get(cell, spec);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(Cell.FOCUSABLE, true);

        super.provideProperties(cell, collector);
      }
    };
  }

  public static CellTrait textEditing() {
    return new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return new TextEditingTrait();
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == CellStateHandler.PROPERTY) {
          return STATE_HANDLER;
        }

        return super.get(cell, spec);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(Cell.FOCUSABLE, true);

        super.provideProperties(cell, collector);
      }
    };
  }

  public static CellTrait validTextEditing(final Predicate<String> validator, final Color validColor, final boolean selectionAvailable) {
    return new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return validTextEditing(validator, validColor);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextNavigationTrait.SELECTION_AVAILABLE) {
          return selectionAvailable;
        }

        return super.get(cell, spec);
      }
    };
  }

  public static CellTrait validTextEditing(final Predicate<String> validator, final Color validColor) {
    return new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return validTextEditing(validator);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == ValidTextEditingTrait.VALID_TEXT_COLOR) {
          return validColor;
        }

        return super.get(cell, spec);
      }
    };
  }

  public static CellTrait validTextEditing(final Predicate<String> validator) {
    return new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return new ValidTextEditingTrait();
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == ValidTextEditingTrait.VALIDATOR) {
          return validator;
        }

        if (spec == CellStateHandler.PROPERTY) {
          return new TextEditorStateHandler(true, validator);
        }

        return super.get(cell, spec);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(Cell.FOCUSABLE, true);

        super.provideProperties(cell, collector);
      }
    };
  }

  private static Function<Cell, Function<String, Runnable>> expansionProvider(final Side side) {
    return new Function<Cell, Function<String, Runnable>>() {
      @Override
      public Function<String, Runnable> apply(final Cell cell) {
        return new Function<String, Runnable>() {
          @Override
          public Runnable apply(String sideText) {
            TextCell popup = CompletionSupport.showSideTransformPopup(cell, side.getPopup(cell), cell.get(side.getKey()), false);
            popup.text().set(sideText);
            return CellActions.toEnd(popup);
          }
        };
      }
    };
  }

  private static class TextEditorStateHandler implements CellStateHandler<Cell, TextEditorCellState> {
    private boolean mySaveText;
    private Predicate<String> myValidator;

    private TextEditorStateHandler(boolean saveText, Predicate<String> validator) {
      mySaveText = saveText;
      myValidator = validator;
    }

    @Override
    public boolean synced(Cell cell) {
      return myValidator == null || myValidator.apply(textEditor(cell).text().get());
    }

    @Override
    public TextEditorCellState saveState(Cell cell) {
      TextEditorCellState result = new TextEditorCellState();
      CellTextEditor editor = textEditor(cell);
      if (mySaveText) {
        String text = editor.text().get();
        result.myText = text;
        result.myValid = myValidator.apply(text);
      }
      result.myCaretPosition = editor.caretPosition().get();
      return result;
    }

    @Override
    public void restoreState(Cell cell, TextEditorCellState state) {
      CellTextEditor editor = textEditor(cell);
      if (mySaveText) {
        editor.text().set(state.myText);
      }
      editor.caretPosition().set(state.myCaretPosition);
    }
  }

  private static class TextEditorCellState implements CellState {
    private int myCaretPosition;
    private String myText;
    private boolean myValid = true;

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof TextEditorCellState)) return false;

      TextEditorCellState otherState = (TextEditorCellState) o;

      return Objects.equal(myText, otherState.myText) && myCaretPosition == otherState.myCaretPosition
          && myValid == otherState.myValid;
    }

    @Override
    public int hashCode() {
      int result = myCaretPosition;
      result = 31 * result + (myText != null ? myText.hashCode() : 0);
      result = 31 * result + (myValid ? 1 : 0);
      return result;
    }

    @Override
    public CellStateDifference getDifference(CellState state) {
      if (!(state instanceof TextEditorCellState)) {
        return myValid ? CellStateDifference.NAVIGATION : CellStateDifference.EDIT;
      }
      TextEditorCellState textState = (TextEditorCellState) state;
      if (!Objects.equal(myText, textState.myText)) {
        return CellStateDifference.EDIT;
      }
      if (myCaretPosition != textState.myCaretPosition) {
        return CellStateDifference.NAVIGATION;
      }
      return CellStateDifference.EQUAL;
    }

    @Override
    public String toString() {
      return "text = '" + myText + "' caret pos = " + myCaretPosition + " valid = " + myValid;
    }
  }
}