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
package jetbrains.jetpad.cell.text;

import com.google.common.base.Predicate;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.CellCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.Side;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.BaseCompletionParameters;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.event.*;
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
    collector.add(TextCell.TEXT_COLOR, cell.get(VALID_TEXT_COLOR));
    super.provideProperties(cell, collector);
  }

  @Override
  public void onAdd(Cell cell) {
    validate(cell, null);
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextCell editor = (TextCell) cell;
    if (event.is(Key.ENTER) && !editor.isEmpty() && !isValid(editor)) {
      CompletionItems completionItems = new CompletionItems(cell.get(Completion.COMPLETION).get(CompletionParameters.EMPTY));
      String prefixText = editor.getPrefixText();
      if (completionItems.hasSingleMatch(prefixText, true)) {
        completionItems.completeFirstMatch(prefixText);
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

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == TextCell.TEXT) {
      validate(cell, new PropertyChangeEventWrapper<>(e));
    }
    super.onPropertyChanged(cell, prop, e);
  }

  private void validate(Cell cell, Event cause) {
    TextCell editor = (TextCell) cell;
    boolean valid = isValid(editor);
    MessageController.setBroken(cell, valid ? null : "Cannot resolve '" + TextEditing.nonNullText(editor) + '\'');
    if (!valid) {
      Event becameInvalidEvent = cause != null ? new DerivedEvent(cause) : new Event();
      cell.dispatch(becameInvalidEvent, Cells.BECAME_INVALID);
    }
  }

  @Override
  protected boolean onAfterType(TextCell editor) {
    if (super.onAfterType(editor)) return true;

    return onAfterTextAdded(editor, CompletionParameters.EMPTY, true);
  }

  @Override
  protected boolean onAfterPaste(TextCell editor) {
    if (super.onAfterPaste(editor)) return true;

    CompletionParameters requireBulkCompletion = new BaseCompletionParameters() {
      @Override
      public boolean isBulkCompletionRequired() {
        return true;
      }
    };

    return onAfterTextAdded(editor, requireBulkCompletion, false);
  }

  private boolean onAfterTextAdded(TextCell editor, CompletionParameters cp, boolean sideTransformationsAllowed) {
    Boolean eagerCompletion = editor.get(TextEditing.EAGER_COMPLETION);
    if (isValid(editor) && !eagerCompletion) return false;

    String text = editor.text().get();
    if (text == null || text.isEmpty()) return false;

    //simple validation
    CompletionItems completion = Completion.completionFor(editor, cp);
    if (completion.hasSingleMatch(text, eagerCompletion)) {
      completion.completeFirstMatch(text);
      return true;
    }

    if (sideTransformationsAllowed) {
      CellContainer container = editor.getContainer();
      if (editor.isEnd() && !completion.hasMatches(text)) {
        //right transform
        String prefix = text.substring(0, text.length() - 1);
        String suffix = text.substring(text.length() - 1);

        if (getValidator(editor).apply(prefix)) {
          handleSideTransform(editor, prefix, suffix.trim(), Side.RIGHT);
        } else {
          List<CompletionItem> matches = completion.matches(prefix);
          if (matches.size() == 1) {
            matches.get(0).complete(prefix).run();
            assertValid(container.focusedCell.get());
            container.keyTyped(new KeyEvent(Key.UNKNOWN, suffix.charAt(0), Collections.<ModifierKey>emptySet()));
          }
        }
      } else if (editor.caretPosition().get() == 1 && !CellCompletionController.isCompletionActive(editor)) {
        //left transform
        String prefix = text.substring(0, 1).trim();
        String suffix = text.substring(1);
        if (getValidator(editor).apply(suffix)) {
          handleSideTransform(editor, suffix, prefix, Side.LEFT);
        }
      }
    }
    return true;
  }

  private void handleSideTransform(TextCell editor, String cellText, String sideText, Side side) {
    CompletionItems sideCompletion = Completion.completionFor(editor, CompletionParameters.EMPTY, side.getKey());
    if (sideCompletion.hasSingleMatch(sideText, editor.get(TextEditing.EAGER_COMPLETION))) {
      setText(editor, cellText);
      sideCompletion.completeFirstMatch(sideText);
    } else {
      if (!sideCompletion.hasMatches(sideText)) return;

      setText(editor, cellText);
      expand(editor, side, sideText).run();
    }
  }

  private Runnable expand(TextCell editor, Side side, String sideText) {
    return side.getExpander(editor).apply(sideText);
  }

  @Override
  protected void onAfterDelete(TextCell editor) {
    super.onAfterDelete(editor);

    if (editor.getParent() == null) return;
    if (!editor.get(TextEditing.EAGER_COMPLETION)) return;

    if (isValid(editor)) return;
    String text = editor.text().get();

    if (text.isEmpty()) return;

    int caret = editor.caretPosition().get();
    CellContainer cellContainer = editor.getContainer();
    CompletionItems completion = Completion.completionFor(editor, CompletionParameters.EMPTY);
    if (completion.hasSingleMatch(text, editor.get(TextEditing.EAGER_COMPLETION))) {
      completion.completeFirstMatch(text);
      Cell focusedCell = cellContainer.focusedCell.get();
      if (focusedCell instanceof TextCell) {
        TextCell focusedEditor = (TextCell) focusedCell;
        if (caret <= focusedEditor.text().get().length()) {
          focusedEditor.caretPosition().set(caret);
        }
      }
    }
  }

  private Predicate<String> getValidator(TextCell editor) {
    return editor.get(VALIDATOR);
  }

  private boolean isValid(TextCell editor) {
    return getValidator(editor).apply(editor.text().get());
  }

  private void assertValid(Cell cell) {
    if (cell instanceof TextCell && !isValid((TextCell) cell)) {
      throw new IllegalStateException("Completion should lead to a valid result, otherwise, we might have a stackoverflow error");
    }
  }
}