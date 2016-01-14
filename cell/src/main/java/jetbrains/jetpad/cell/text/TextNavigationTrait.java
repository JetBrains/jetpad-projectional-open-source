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

  static int getMinPos(TextEditorCell t) {
    return isFirstAllowed(t) ? 0 : 1;
  }

  static int getMaxPos(TextEditorCell t) {
    int last = TextEditing.lastPosition(t);
    return isLastAllowed(t) ? last : last - 1;
  }

  private static Boolean isFirstAllowed(TextEditorCell t) {
    return ((Cell) t).get(TextEditing.FIRST_ALLOWED);
  }

  private static Boolean isLastAllowed(TextEditorCell t) {
    return ((Cell) t).get(TextEditing.LAST_ALLOWED);
  }

  private boolean isSelectionAvailable(Cell cell) {
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

  @Override
  public void onKeyPressed(Cell c, KeyEvent event) {
    TextEditorCell cell = (TextEditorCell) c;
    String currentText = TextEditing.text(cell);
    int caret = cell.caretPosition().get();
    int minCaret = isFirstAllowed(cell) ? 0 : 1;
    int textLen = currentText.length();
    int maxCaret = isLastAllowed(cell) ? textLen : textLen - 1;


    boolean selectionAvailable = isSelectionAvailable(c);

    boolean selection = event.has(ModifierKey.SHIFT);
    try {
      if (isCaretKey(selectionAvailable, event, Key.LEFT) && caret > minCaret) {
        cell.caretPosition().set(caret - 1);
        cell.scrollToCaret();
        event.consume();
        return;
      }

      if (isCaretKey(selectionAvailable, event, Key.RIGHT) && caret < maxCaret) {
        cell.caretPosition().set(caret + 1);
        cell.scrollToCaret();
        event.consume();
        return;
      }

      if ((isCaretKey(selectionAvailable, event, Key.LEFT, ModifierKey.ALT) ||
          event.is(KeyStrokeSpecs.HOME) ||
          (event.is(KeyStrokeSpecs.SELECT_HOME) && selectionAvailable) ||
          event.is(KeyStrokeSpecs.PREV_WORD)) && caret > 0) {
        cell.caretPosition().set(0);
        cell.scrollToCaret();
        event.consume();
        return;
      }

      if ((isCaretKey(selectionAvailable, event, Key.RIGHT, ModifierKey.ALT) ||
          event.is(KeyStrokeSpecs.END) ||
          (event.is(KeyStrokeSpecs.SELECT_END) && selectionAvailable) ||
          event.is(KeyStrokeSpecs.NEXT_WORD_ALT)) && caret < maxCaret ||
          event.is(KeyStrokeSpecs.NEXT_WORD_CONTROL.with(ModifierKey.SHIFT))) {
        cell.caretPosition().set(maxCaret);
        cell.scrollToCaret();
        event.consume();
        return;
      }

    } finally {
      if (event.isConsumed()) {
        if (cell.selectionVisible().get() && !selection) {
          cell.selectionVisible().set(false);
        }
        if (!cell.selectionVisible().get() && selection) {
          cell.selectionStart().set(caret);
          cell.selectionVisible().set(true);
        }
      }
    }

    if (event.is(KeyStrokeSpecs.SELECT_ALL)) {
      cell.selectionStart().set(0);
      cell.caretPosition().set(maxCaret);
      cell.selectionVisible().set(true);
      cell.scrollToCaret();
      event.consume();
      return;
    }

    super.onKeyPressed(c, event);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == Cell.FOCUSED) {
      TextEditorCell textEditorCell = (TextEditorCell) cell;
      PropertyChangeEvent<Boolean> event = (PropertyChangeEvent<Boolean>) e;

      textEditorCell.caretVisible().set(event.getNewValue());
      if (!event.getNewValue()) {
        textEditorCell.selectionVisible().set(false);
      }

      if (!event.getNewValue()) return;

      int caret = textEditorCell.caretPosition().get();
      String text = TextEditing.text(textEditorCell);

      if (!isFirstAllowed(textEditorCell) && caret == 0) {
        textEditorCell.caretPosition().set(1);
      }

      if (!isLastAllowed(textEditorCell) && caret == text.length()) {
        textEditorCell.caretPosition().set(text.length() - 1);
      }
    }

    if (prop == TextEditorCell.TEXT) {
      TextEditorCell textEditorCell = (TextEditorCell) cell;
      PropertyChangeEvent<String> event = (PropertyChangeEvent<String>) e;
      int newLength = event.getNewValue() != null ? event.getNewValue().length() : 0;
      if (textEditorCell.caretPosition().get() > newLength) {
        textEditorCell.caretPosition().set(newLength);
      }
    }

    super.onPropertyChanged(cell, prop, e);
  }

  @Override
  public void onKeyTypedLowPriority(Cell cell, KeyEvent event) {
    String s = ("" + event.getKeyChar()).trim();
    if (TextEditing.isEnd((TextEditorCell) cell) && showCompletion(cell, s, Completion.RIGHT_TRANSFORM, cell.rightPopup()) ) {
      event.consume();
    }
    if (TextEditing.isHome((TextEditorCell) cell) && showCompletion(cell, s, Completion.LEFT_TRANSFORM, cell.leftPopup())) {
      event.consume();
    }

    super.onKeyTypedLowPriority(cell, event);
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    CompletionItems completion = Completion.completionFor(cell, CompletionParameters.EMPTY, Completion.COMPLETION);
    if (completion.isEmpty()) {
      if (cell.get(Cell.RIGHT_POPUP) == null && TextEditing.isEnd((TextEditorCell) cell) &&
        showSideCompletion((TextEditorCell) cell, Completion.RIGHT_TRANSFORM, cell.rightPopup())) {
        event.consume();
        return;
      }
      if (cell.get(Cell.LEFT_POPUP) == null && TextEditing.isHome((TextEditorCell) cell) &&
        showSideCompletion((TextEditorCell) cell, Completion.LEFT_TRANSFORM, cell.leftPopup())) {
        event.consume();
        return;
      }
    }

    super.onComplete(cell, event);
  }

  private boolean showSideCompletion(final TextEditorCell textEditorCell, CellTraitPropertySpec<CompletionSupplier> key, Property<Cell> popupProp) {
    CompletionItems completion = Completion.completionFor((Cell) textEditorCell, CompletionParameters.EMPTY, key);
    if (!completion.isEmpty()) {
      final TextCell popup = CompletionSupport.showSideTransformPopup(((Cell) textEditorCell), popupProp, ((Cell) textEditorCell).get(key), false);
      final CompletionController controller = popup.get(Completion.COMPLETION_CONTROLLER);
      controller.activate(new Runnable() {
        @Override
        public void run() {
          ((Cell) textEditorCell).focus();
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
        popup.caretPosition().set(TextEditing.lastPosition(popup));
      }
      return true;
    }
    return false;
  }

  @Override
  public void onMousePressed(Cell c, MouseEvent event) {
    if (!c.focusable().get()) return;
    TextEditorCell cell = (TextEditorCell) c;
    int pos = getPosAt(cell, event);
    cell.caretPosition().set(pos);
    c.focus();
    cell.selectionVisible().set(false);
    event.consume();
  }

  @Override
  public void onMouseDragged(Cell c, MouseEvent event) {
    if (!c.focusable().get()) return;
    TextEditorCell cell = (TextEditorCell) c;
    int pos = getPosAt(cell, event);
    cell.selectionVisible().set(true);
    cell.selectionStart().set(pos);
    event.consume();
  }

  private int getPosAt(TextEditorCell cell, MouseEvent event) {
    int max = getMaxPos(cell);
    int min = getMinPos(cell);
    int offset = event.getX() - ((Cell) cell).origin().x;
    return Math.max(min, Math.min(cell.getCaretAt(offset), max));
  }
}