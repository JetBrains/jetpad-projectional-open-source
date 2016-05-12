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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

class TextNavigationTrait extends CellTrait {
  static final CellTraitPropertySpec<Boolean> SELECTION_AVAILABLE = new CellTraitPropertySpec<>("selectionAvailable", true);

  private static boolean isFirstAllowed(TextCell cell) {
    return cell.get(TextEditing.FIRST_ALLOWED);
  }

  private static boolean isLastAllowed(TextCell cell) {
    return cell.get(TextEditing.LAST_ALLOWED);
  }

  static int getMinPos(TextCell t) {
    return isFirstAllowed(t) ? 0 : 1;
  }

  static int getMaxPos(TextCell t) {
    int last = t.lastPosition();
    return isLastAllowed(t) ? last : last - 1;
  }

  private boolean isSelectionAvailable(TextCell cell) {
    return cell.get(SELECTION_AVAILABLE);
  }

  private boolean isCaretKey(boolean selectionAvailable, KeyEvent event, Key key, ModifierKey... modifiers) {
    if (event.is(key, modifiers)) return true;
    if (!selectionAvailable) return false;

    ModifierKey[] mods = new ModifierKey[modifiers.length + 1];
    System.arraycopy(modifiers, 0, mods, 0, modifiers.length);
    mods[modifiers.length] = ModifierKey.SHIFT;

    return event.is(key, mods);
  }

  private boolean isCellHome(boolean selectionAvailable, KeyEvent event) {
    return event.is(KeyStrokeSpecs.PREV_WORD)
        || isCaretKey(selectionAvailable, event, Key.LEFT, ModifierKey.ALT);
  }

  private boolean isCellEnd(boolean selectionAvailable, KeyEvent event) {
    return event.is(KeyStrokeSpecs.NEXT_WORD)
        || isCaretKey(selectionAvailable, event, Key.RIGHT, ModifierKey.ALT);
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextCell editor = (TextCell) cell;
    String currentText = TextEditing.nonNullText(editor);
    int caret = editor.caretPosition().get();
    int minCaret = getMinPos(editor);
    int textLen = currentText.length();
    int maxCaret = isLastAllowed(editor) ? textLen : textLen - 1;

    boolean selectionAvailable = isSelectionAvailable(editor);

    boolean selection = event.has(ModifierKey.SHIFT);
    try {
      if (isCaretKey(selectionAvailable, event, Key.LEFT) && caret > minCaret) {
        editor.caretPosition().set(caret - 1);
        editor.scrollToCaret();
        event.consume();
        return;
      }

      if (isCaretKey(selectionAvailable, event, Key.RIGHT) && caret < maxCaret) {
        editor.caretPosition().set(caret + 1);
        editor.scrollToCaret();
        event.consume();
        return;
      }

      if ((caret > 0 && isCellHome(selectionAvailable, event)) ||
          (selectionAvailable && event.is(KeyStrokeSpecs.SELECT_HOME)) ||
          event.is(KeyStrokeSpecs.PREV_WORD_CONTROL.with(ModifierKey.SHIFT))) {
        editor.caretPosition().set(0);
        editor.scrollToCaret();
        event.consume();
        return;
      }

      if ((caret < maxCaret && isCellEnd(selectionAvailable, event)) ||
          (selectionAvailable && event.is(KeyStrokeSpecs.SELECT_END)) ||
          event.is(KeyStrokeSpecs.NEXT_WORD_CONTROL.with(ModifierKey.SHIFT))) {
        editor.caretPosition().set(maxCaret);
        editor.scrollToCaret();
        event.consume();
        return;
      }

    } finally {
      if (event.isConsumed()) {
        if (editor.selectionVisible().get() && !selection) {
          editor.selectionVisible().set(false);
        }
        if (!editor.selectionVisible().get() && selection) {
          editor.selectionStart().set(caret);
          editor.selectionVisible().set(true);
        }
      }
    }

    if (event.is(KeyStrokeSpecs.SELECT_ALL)) {
      editor.selectionStart().set(0);
      editor.caretPosition().set(maxCaret);
      editor.selectionVisible().set(true);
      editor.scrollToCaret();
      event.consume();
      return;
    }

    super.onKeyPressed(cell, event);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == Cell.FOCUSED) {
      TextCell editor = (TextCell) cell;
      PropertyChangeEvent<Boolean> event = (PropertyChangeEvent<Boolean>) e;

      editor.caretVisible().set(event.getNewValue());
      if (!event.getNewValue()) {
        editor.selectionVisible().set(false);
      }

      if (!event.getNewValue()) return;

      int caret = editor.caretPosition().get();
      String text = TextEditing.nonNullText(editor);

      if (!isFirstAllowed(editor) && caret == 0) {
        editor.caretPosition().set(1);
      }

      if (!isLastAllowed(editor) && caret == text.length()) {
        editor.caretPosition().set(text.length() - 1);
      }
    }

    if (prop == TextCell.TEXT) {
      TextCell editor = (TextCell) cell;
      PropertyChangeEvent<String> event = (PropertyChangeEvent<String>) e;
      int newLength = event.getNewValue() != null ? event.getNewValue().length() : 0;
      if (editor.caretPosition().get() > newLength) {
        editor.caretPosition().set(newLength);
      }
    }

    super.onPropertyChanged(cell, prop, e);
  }

  @Override
  public void onKeyTypedLowPriority(Cell cell, KeyEvent event) {
    TextCell editor = (TextCell) cell;
    String s = ("" + event.getKeyChar()).trim();
    if (editor.isEnd() && showCompletion(editor, s, Completion.RIGHT_TRANSFORM, editor.rightPopup()) ) {
      event.consume();
    }
    if (editor.isHome() && showCompletion(editor, s, Completion.LEFT_TRANSFORM, editor.leftPopup())) {
      event.consume();
    }

    super.onKeyTypedLowPriority(cell, event);
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    CompletionItems completion = Completion.completionFor(cell, CompletionParameters.EMPTY, Completion.COMPLETION);
    TextCell editor = (TextCell) cell;
    if (completion.isEmpty()) {
      if (cell.get(Cell.RIGHT_POPUP) == null && editor.isEnd() &&
        showSideCompletion(editor, Completion.RIGHT_TRANSFORM, editor.rightPopup())) {
        event.consume();
        return;
      }
      if (cell.get(Cell.LEFT_POPUP) == null && editor.isHome() &&
        showSideCompletion(editor, Completion.LEFT_TRANSFORM, editor.leftPopup())) {
        event.consume();
        return;
      }
    }

    super.onComplete(cell, event);
  }

  private boolean showSideCompletion(final Cell cell, CellTraitPropertySpec<CompletionSupplier> key, Property<Cell> popupProp) {
    CompletionItems completion = Completion.completionFor(cell, CompletionParameters.EMPTY, key);
    if (!completion.isEmpty()) {
      final TextCell popup = CompletionSupport.showSideTransformPopup(cell, popupProp, cell.get(key), false);
      final CompletionController controller = popup.get(Completion.COMPLETION_CONTROLLER);
      controller.activate(new Runnable() {
        @Override
        public void run() {
          cell.focus();
        }
      });
      return true;
    }
    return false;
  }

  private boolean showCompletion(Cell cell, String text, CellTraitPropertySpec<CompletionSupplier> key, Property<Cell> popupProp) {
    CompletionItems completion = Completion.completionFor(cell, CompletionParameters.EMPTY, key);
    if (completion.hasMatches(text)) {
      if (completion.hasSingleMatch(text, cell.get(TextEditing.EAGER_COMPLETION))) {
        completion.completeFirstMatch(text);
      } else {
        TextCell popup = CompletionSupport.showSideTransformPopup(cell, popupProp, cell.get(key), false);
        popup.text().set(text);
        popup.caretPosition().set(popup.lastPosition());
      }
      return true;
    }
    return false;
  }

  @Override
  public void onMousePressed(Cell cell, MouseEvent event) {
    if (!cell.focusable().get()) return;
    TextCell editor = (TextCell) cell;
    int pos = getPosAt(editor, event);
    editor.caretPosition().set(pos);
    editor.focus();
    editor.selectionVisible().set(false);
    event.consume();
  }

  @Override
  public void onMouseDragged(Cell cell, MouseEvent event) {
    if (!cell.focusable().get()) return;
    TextCell editor = (TextCell) cell;
    int pos = getPosAt(editor, event);
    editor.selectionVisible().set(true);
    editor.selectionStart().set(pos);
    event.consume();
  }

  private int getPosAt(TextCell editor, MouseEvent event) {
    int max = getMaxPos(editor);
    int min = getMinPos(editor);
    int offset = event.getX() - editor.origin().x;
    return Math.max(min, Math.min(editor.getCaretAt(offset), max));
  }
}