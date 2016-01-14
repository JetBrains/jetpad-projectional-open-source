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

import com.google.common.base.Predicate;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.completion.BaseCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.Side;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.Collections;
import java.util.List;

class ValidTextEditingTrait extends TextEditingTrait {
  static final CellTraitPropertySpec<Predicate<String>> VALIDATOR = new CellTraitPropertySpec<>("validator");
  static final CellTraitPropertySpec<Color> VALID_TEXT_COLOR = new CellTraitPropertySpec<>("validTextColor", Color.BLACK);

  ValidTextEditingTrait() {
  }

  @Override
  protected void provideProperties(Cell cell, PropertyCollector collector) {
    collector.add(TextEditorCell.TEXT_COLOR, cell.get(VALID_TEXT_COLOR));
    super.provideProperties(cell, collector);
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextEditorCell textEditorCell = (TextEditorCell) cell;
    if (event.is(Key.ENTER) && !TextEditing.isEmpty(textEditorCell) && !isValid(textEditorCell)) {
      CompletionItems completionItems = new CompletionItems(cell.get(Completion.COMPLETION).get(CompletionParameters.EMPTY));
      String prefixText = TextEditing.getPrefixText(textEditorCell);
      if (completionItems.hasSingleMatch(prefixText, true)) {
        completionItems.completeFirstMatch(prefixText);
        event.consume();
        return;
      }
    }

    super.onKeyPressed(cell, event);
  }

  @Override
  protected boolean canCompleteWithCtrlSpace(TextEditorCell cell) {
    return !isValid(cell);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == TextEditorCell.TEXT) {
      TextEditorCell textEditorCell = (TextEditorCell) cell;
      MessageController.setBroken(cell, isValid(textEditorCell) ? null : "Cannot resolve '" + TextEditing.text(textEditorCell) + '\'');
    }

    super.onPropertyChanged(cell, prop, e);
  }

  @Override
  protected boolean onAfterType(TextEditorCell textEditorCell) {
    if (super.onAfterType(textEditorCell)) return true;

    Boolean eagerCompletion = ((Cell) textEditorCell).get(TextEditing.EAGER_COMPLETION);
    if (isValid(textEditorCell) && !eagerCompletion) return false;

    String text = textEditorCell.text().get();
    if (text == null || text.isEmpty()) return false;

    //simple validation
    CompletionItems completion = Completion.completionFor(((Cell) textEditorCell), CompletionParameters.EMPTY);
    if (completion.hasSingleMatch(text, eagerCompletion)) {
      completion.completeFirstMatch(text);
      return true;
    }

    CellContainer container = ((Cell) textEditorCell).getContainer();
    if (TextEditing.isEnd(textEditorCell) && !completion.hasMatches(text)) {
      //right transform
      String prefix = text.substring(0, text.length() - 1);
      String suffix = text.substring(text.length() - 1);

      if (getValidator(textEditorCell).apply(prefix)) {
        handleSideTransform(textEditorCell, prefix, suffix.trim(), Side.RIGHT);
      } else {
        List<CompletionItem> matches = completion.matches(prefix);
        if (matches.size() == 1) {
          matches.get(0).complete(prefix).run();
          assertValid(container.focusedCell.get());
          container.keyTyped(new KeyEvent(Key.UNKNOWN, suffix.charAt(0), Collections.<ModifierKey>emptySet()));
        }
      }
    } else if (textEditorCell.caretPosition().get() == 1 && !BaseCompletionController.isCompletionActive(((Cell) textEditorCell))) {
      //left transform
      String prefix = text.substring(0, 1).trim();
      String suffix = text.substring(1);
      if (getValidator(textEditorCell).apply(suffix)) {
        handleSideTransform(textEditorCell, suffix, prefix, Side.LEFT);
      }
    }
    return true;
  }

  private void handleSideTransform(TextEditorCell textEditorCell, String cellText, String sideText, Side side) {
    CompletionItems sideCompletion = Completion.completionFor(((Cell) textEditorCell), CompletionParameters.EMPTY, side.getKey());
    if (sideCompletion.hasSingleMatch(sideText, ((Cell) textEditorCell).get(TextEditing.EAGER_COMPLETION))) {
      setText(textEditorCell, cellText);
      sideCompletion.completeFirstMatch(sideText);
    } else {
      if (!sideCompletion.hasMatches(sideText)) return;

      setText(textEditorCell, cellText);
      expand(textEditorCell, side, sideText).run();
    }
  }

  private Runnable expand(TextEditorCell textEditorCell, Side side, String sideText) {
    return side.getExpander(((Cell) textEditorCell)).apply(sideText);
  }

  @Override
  protected void onAfterDelete(TextEditorCell cell) {
    super.onAfterDelete(cell);

    if (!((Cell) cell).get(TextEditing.EAGER_COMPLETION)) return;

    if (isValid(cell)) return;
    String text = cell.text().get();

    if (text.isEmpty()) return;

    int caret = cell.caretPosition().get();
    CellContainer cellContainer = ((Cell) cell).getContainer();
    CompletionItems completion = Completion.completionFor(((Cell) cell), CompletionParameters.EMPTY);
    if (completion.hasSingleMatch(text, ((Cell) cell).get(TextEditing.EAGER_COMPLETION))) {
      completion.completeFirstMatch(text);
      Cell focusedCell = cellContainer.focusedCell.get();
      if (focusedCell instanceof TextEditorCell && caret <= ((TextEditorCell) focusedCell).text().get().length()) {
        ((TextEditorCell) focusedCell).caretPosition().set(caret);
      }
    }
  }

  private Predicate<String> getValidator(TextEditorCell cell) {
    return ((Cell) cell).get(VALIDATOR);
  }

  private boolean isValid(TextEditorCell cell) {
    return getValidator(cell).apply(cell.text().get());
  }

  private void assertValid(Cell cell) {
    if (cell != null && cell instanceof TextEditorCell && !isValid((TextEditorCell) cell)) {
      throw new IllegalStateException("Completion should lead to a valid result, otherwise, we might have a stackoverflow error");
    }
  }
}