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
package jetbrains.jetpad.projectional.cell.support;

import com.google.common.base.Predicate;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.projectional.cell.*;
import jetbrains.jetpad.projectional.cell.action.CellAction;
import jetbrains.jetpad.projectional.cell.completion.Completion;
import jetbrains.jetpad.projectional.cell.completion.CompletionHelper;
import jetbrains.jetpad.projectional.cell.completion.CompletionItem;
import jetbrains.jetpad.projectional.cell.completion.CompletionParameters;
import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.values.Color;

import java.util.Collections;
import java.util.List;

class ValidTextEditingTrait extends TextEditingTrait {
  static final CellTraitPropertySpec<Predicate<String>> VALIDATOR = new CellTraitPropertySpec<Predicate<String>>("validator");
  static final CellTraitPropertySpec<Color> VALID_TEXT_COLOR = new CellTraitPropertySpec<Color>("validTextColor", Color.BLACK);

  ValidTextEditingTrait() {
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextCell textCell = (TextCell) cell;
    if (event.is(Key.ENTER) && !isEmpty(textCell) && !isValid(textCell)) {
      CompletionHelper completionHelper = new CompletionHelper(textCell.get(Completion.COMPLETION).get(CompletionParameters.EMPTY));
      String prefixText = textCell.prefixText().get();
      if (completionHelper.hasSingleMatch(prefixText, true)) {
        completionHelper.completeFirstMatch(prefixText);
        event.consume();
        return;
      }
    }

    super.onKeyPressed(cell, event);
  }

  @Override
  protected boolean canCompleteWithCtrlSpace(TextCell cell) {
    return !isValid(cell);
  }

  private boolean isEmpty(TextCell cell) {
    return cell.text().get() == null || cell.text().get().length() == 0;
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == TextCell.TEXT) {
      TextCell textCell = (TextCell) cell;
      textCell.textColor().set(isValid(textCell) ? textCell.get(VALID_TEXT_COLOR) : Color.RED);
    }

    super.onPropertyChanged(cell, prop, e);
  }

  private boolean isValid(TextCell textCell) {
    Predicate<String> validator = textCell.get(VALIDATOR);
    return validator.apply(textCell.text().get());
  }

  @Override
  protected boolean onAfterType(TextCell textCell) {
    if (super.onAfterType(textCell)) return true;

    Boolean eagerCompletion = textCell.get(TextEditing.EAGER_COMPLETION);
    if (getValidator(textCell).apply(textCell.text().get()) && !eagerCompletion) return false;

    String text = textCell.text().get();
    if (text == null || text.isEmpty()) return false;

    //simple validation
    CompletionHelper completion = CompletionHelper.completionFor(textCell, CompletionParameters.EMPTY);
    if (eagerCompletion && completion.hasSingleMatch(text, true)) {
      completion.completeFirstMatch(text);
      return true;
    }

    CellContainer container = textCell.container();
    if (textCell.isEnd()) {
      String prefix = text.substring(0, text.length() - 1);
      String suffix = text.substring(text.length() - 1).trim();

      if (completion.hasSingleMatch(text, eagerCompletion)) {
        completion.completeFirstMatch(text);
      } else if (!completion.hasMatches(text)) {
        //right transform
        if (getValidator(textCell).apply(prefix)) {
          handleSideTransform(textCell, prefix, suffix, Side.RIGHT);
        } else {
          List<CompletionItem> matches = completion.matches(prefix);
          if (matches.size() == 1) {
            matches.get(0).complete(prefix).execute();
            container.keyTyped(new KeyEvent(Key.UNKNOWN, suffix.charAt(0), Collections.<ModifierKey>emptySet()));
          }
        }
      }
    } else if (textCell.caretPosition().get() == 1 && !(textCell.bottomPopup().get() != null)) {
      //left transform
      String prefix = text.substring(0, 1).trim();
      String suffix = text.substring(1);
      if (getValidator(textCell).apply(suffix)) {
        handleSideTransform(textCell, suffix, prefix, Side.LEFT);
      }
    }
    return true;
  }

  private void handleSideTransform(TextCell textCell, String cellText, String sideText, Side side) {
    CompletionHelper sideCompletion = CompletionHelper.completionFor(textCell, CompletionParameters.EMPTY, side.getKey());
    if (sideCompletion.hasSingleMatch(sideText, textCell.get(TextEditing.EAGER_COMPLETION))) {
      setText(textCell, cellText);
      sideCompletion.completeFirstMatch(sideText);
    } else {
      if (!sideCompletion.hasMatches(sideText)) return;

      setText(textCell, cellText);
      expand(textCell, side, sideText).execute();
    }
  }

  private CellAction expand(TextCell textCell, Side side, String sideText) {
    return side.getExpander(textCell).apply(sideText);
  }

  @Override
  protected void onAfterDelete(TextCell textCell) {
    super.onAfterDelete(textCell);

    if (!textCell.get(TextEditing.EAGER_COMPLETION)) return;

    if (getValidator(textCell).apply(textCell.text().get())) return;
    String text = textCell.text().get();

    if (text.isEmpty()) return;

    int caret = textCell.caretPosition().get();
    CellContainer cellContainer = textCell.container();
    CompletionHelper completion = CompletionHelper.completionFor(textCell, CompletionParameters.EMPTY);
    if (completion.hasSingleMatch(text, textCell.get(TextEditing.EAGER_COMPLETION))) {
      completion.completeFirstMatch(text);
      Cell focusedCell = cellContainer.focusedCell.get();
      if (focusedCell instanceof TextCell && caret <= ((TextCell) focusedCell).text().get().length()) {
        ((TextCell) focusedCell).caretPosition().set(caret);
      }
    }
  }

  private Predicate<String> getValidator(TextCell textCell) {
    return textCell.get(VALIDATOR);
  }
}