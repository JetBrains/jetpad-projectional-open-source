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
import jetbrains.jetpad.cell.completion.CompletionHelper;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.completion.Side;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateDifference;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.completion.CompletionParameters;
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

  private static final TextCellStateHandler TEXT_CELL_STATE_HANDLER = new TextCellStateHandler(false, null);

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
          return TEXT_CELL_STATE_HANDLER;
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
          return TEXT_CELL_STATE_HANDLER;
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
        return validTextEditing(validator);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == ValidTextEditingTrait.VALID_TEXT_COLOR) {
          return validColor;
        }

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
          return new TextCellStateHandler(true, validator);
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
            CompletionHelper sideCompletion = CompletionHelper.completionFor(cell, CompletionParameters.EMPTY, side.getKey());
            TextCell popupCell = CompletionSupport.showSideTransformPopup(cell, side.getPopup(cell), sideCompletion.getItems());
            popupCell.text().set(sideText);
            return CellActions.toEnd(popupCell);
          }
        };
      }
    };
  }

  private static class TextCellStateHandler implements CellStateHandler<TextCell, TextCellState> {
    private boolean mySaveText;
    private Predicate<String> myValidator;

    private TextCellStateHandler(boolean saveText, Predicate<String> validator) {
      mySaveText = saveText;
      myValidator = validator;
    }

    @Override
    public boolean synced(TextCell cell) {
      if (myValidator == null) {
        return true;
      }
      return myValidator.apply(cell.text().get());
    }

    @Override
    public TextCellState saveState(TextCell cell) {
      TextCellState result = new TextCellState();
      if (mySaveText) {
        String text = cell.text().get();
        result.myText = text;
        result.myValid = myValidator.apply(text);
      }
      result.myCaretPosition = cell.caretPosition().get();
      return result;
    }

    @Override
    public void restoreState(TextCell cell, TextCellState state) {
      if (mySaveText) {
        cell.text().set(state.myText);
      }
      cell.caretPosition().set(state.myCaretPosition);
    }
  }

  private static class TextCellState implements CellState {
    private int myCaretPosition;
    private String myText;
    private boolean myValid = true;

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof TextCellState)) return false;

      TextCellState otherState = (TextCellState) o;

      return Objects.equal(myText, otherState.myText) && myCaretPosition == otherState.myCaretPosition &&
          myValid == otherState.myValid;
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
      if (!(state instanceof TextCellState)) {
        return myValid ? CellStateDifference.NAVIGATION : CellStateDifference.EDIT;
      }
      TextCellState textState = (TextCellState) state;
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