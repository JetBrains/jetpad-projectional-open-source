/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

class TextNavigationTrait extends CellTrait {
  static final CellTraitPropertySpec<Boolean> SELECTION_AVAILABLE = new CellTraitPropertySpec<>("selectionAvailable", true);

  static int getMinPos(TextCell tv) {
    if (isFirstAllowed(tv)) {
      return 0;
    } else {
      return 1;
    }
  }

  static int getMaxPos(TextCell tv) {
    String text = tv.text().get();
    if (text == null || text.isEmpty()) text = "";
    if (isLastAllowed(tv)) {
      return text.length();
    } else {
      return text.length() - 1;
    }
  }

  static Boolean isFirstAllowed(TextCell tv) {
    return tv.get(TextEditing.FIRST_ALLOWED);
  }

  static Boolean isLastAllowed(TextCell tv) {
    return tv.get(TextEditing.LAST_ALLOWED);
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
    TextCell cell = (TextCell) c;
    String currentText = cell.text().get();
    if (currentText == null) {
      currentText = "";
    }
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

    super.onKeyPressed(cell, event);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == Cell.FOCUSED) {
      TextCell textCell = (TextCell) cell;
      PropertyChangeEvent<Boolean> event = (PropertyChangeEvent<Boolean>) e;

      textCell.caretVisible().set(event.getNewValue());
      if (!event.getNewValue()) {
        textCell.selectionVisible().set(false);
      }

      if (!event.getNewValue()) return;

      int caret = textCell.caretPosition().get();
      String text = textCell.text().get();
      if (text == null) text = "";

      if (!isFirstAllowed(textCell) && caret == 0) {
        textCell.caretPosition().set(1);
      }

      if (!isLastAllowed(textCell) && caret == text.length()) {
        textCell.caretPosition().set(text.length() - 1);
      }
    }

    if (prop == TextCell.TEXT) {
      TextCell textCell = (TextCell) cell;
      PropertyChangeEvent<String> event = (PropertyChangeEvent<String>) e;
      int newLength = event.getNewValue() != null ? event.getNewValue().length() : 0;
      if (textCell.caretPosition().get() > newLength) {
        textCell.caretPosition().set(newLength);
      }
    }

    super.onPropertyChanged(cell, prop, e);
  }

  @Override
  public void onKeyTypedLowPriority(Cell cell, KeyEvent event) {
    TextCell textCell = (TextCell) cell;
    String s = ("" + event.keyChar()).trim();
    if (textCell.isEnd() && showCompletion(textCell, s, Completion.RIGHT_TRANSFORM, textCell.rightPopup()) ) {
      event.consume();
    }

    if (textCell.isHome() && showCompletion(textCell, s, Completion.LEFT_TRANSFORM, textCell.leftPopup())) {
      event.consume();
    }

    super.onKeyTypedLowPriority(textCell, event);
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    final TextCell textCell = (TextCell) cell;

    CompletionHelper completion = CompletionHelper.completionFor(cell, CompletionParameters.EMPTY, Completion.COMPLETION);
    if (completion.isEmpty()) {
      if (textCell.rightPopup().get() == null && textCell.isEnd() &&
        showSideCompletion(textCell, Completion.RIGHT_TRANSFORM, textCell.rightPopup())) {
        event.consume();
        return;
      }

      if (textCell.leftPopup().get() == null && textCell.isHome() &&
        showSideCompletion(textCell, Completion.LEFT_TRANSFORM, textCell.leftPopup())) {
        event.consume();
        return;
      }
    }

    super.onComplete(cell, event);
  }

  private boolean showSideCompletion(final TextCell textCell, CellTraitPropertySpec<CompletionSupplier> key, Property<Cell> popupProp) {
    CompletionHelper completion = CompletionHelper.completionFor(textCell, CompletionParameters.EMPTY, key);
    if (!completion.isEmpty()) {
      final TextCell popup = CompletionSupport.showSideTransformPopup(textCell, popupProp, completion.getItems());
      final CompletionController controller = popup.get(Completion.COMPLETION_CONTROLLER);
      controller.activate(new Runnable() {
        @Override
        public void run() {
          textCell.focus();
        }
      });
      return true;
    }
    return false;
  }


  private boolean showCompletion(Cell cell, String text, CellTraitPropertySpec<CompletionSupplier> key, Property<Cell> popup) {
    CompletionHelper completion = CompletionHelper.completionFor(cell, CompletionParameters.EMPTY, key);
    if (completion.hasMatches(text)) {
      if (completion.hasSingleMatch(text, cell.get(TextEditing.EAGER_COMPLETION))) {
        completion.completeFirstMatch(text);
      } else {
        TextCell popupCell = CompletionSupport.showSideTransformPopup(cell, popup, completion.getItems());
        popupCell.text().set(text);
        popupCell.caretPosition().set(popupCell.text().get().length());
        return true;
      }
    }
    return false;
  }

  @Override
  public void onMousePressed(Cell c, MouseEvent event) {
    TextCell cell = (TextCell) c;
    if (!cell.focusable().get()) return;
    int pos = getPosAt(cell, event);
    cell.caretPosition().set(pos);
    cell.focus();
    cell.selectionVisible().set(false);
    event.consume();
  }

  @Override
  public void onMouseDragged(Cell c, MouseEvent event) {
    TextCell cell = (TextCell) c;
    if (!cell.focusable().get()) return;
    int pos = getPosAt(cell, event);
    cell.selectionVisible().set(true);
    cell.selectionStart().set(pos);
    event.consume();
  }

  private int getPosAt(TextCell cell, MouseEvent event) {
    int max = getMaxPos(cell);
    int min = getMinPos(cell);
    int offset = event.getX() - cell.origin().x;
    return Math.max(min, Math.min(cell.getCaretAt(offset), max));
  }
}