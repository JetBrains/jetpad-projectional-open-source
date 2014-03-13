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
    TextCell view = (TextCell) c;
    String currentText = view.text().get();
    if (currentText == null) {
      currentText = "";
    }
    int caret = view.caretPosition().get();
    int minCaret = isFirstAllowed(view) ? 0 : 1;
    int textLen = currentText.length();
    int maxCaret = isLastAllowed(view) ? textLen : textLen - 1;


    boolean selectionAvailable = isSelectionAvailable(c);

    boolean selection = event.has(ModifierKey.SHIFT);
    try {
      if (isCaretKey(selectionAvailable, event, Key.LEFT) && caret > minCaret) {
        view.caretPosition().set(caret - 1);
        event.consume();
        return;
      }

      if (isCaretKey(selectionAvailable, event, Key.RIGHT) && caret < maxCaret) {
        view.caretPosition().set(caret + 1);
        event.consume();
        return;
      }

      if ((isCaretKey(selectionAvailable, event, Key.LEFT, ModifierKey.ALT) ||
          event.is(KeyStrokeSpecs.HOME) ||
          (event.is(KeyStrokeSpecs.SELECT_HOME) && selectionAvailable) ||
          event.is(KeyStrokeSpecs.PREV_WORD)) && caret > 0) {
        view.caretPosition().set(0);
        event.consume();
        return;
      }

      if ((isCaretKey(selectionAvailable, event, Key.RIGHT, ModifierKey.ALT) ||
          event.is(KeyStrokeSpecs.END) ||
          (event.is(KeyStrokeSpecs.SELECT_END) && selectionAvailable) ||
          event.is(KeyStrokeSpecs.NEXT_WORD_ALT)) && caret < maxCaret ||
          event.is(KeyStrokeSpecs.NEXT_WORD_CONTROL.addModifier(ModifierKey.SHIFT))) {
        view.caretPosition().set(maxCaret);
        event.consume();
        return;
      }
    } finally {
      if (event.isConsumed()) {
        if (view.selectionVisible().get() && !selection) {
          view.selectionVisible().set(false);
        }

        if (!view.selectionVisible().get() && selection) {
          view.selectionStart().set(caret);
          view.selectionVisible().set(true);
        }
      }
    }

    if (event.is(KeyStrokeSpecs.SELECT_ALL)) {
      view.selectionStart().set(0);
      view.caretPosition().set(maxCaret);
      view.selectionVisible().set(true);
      event.consume();
      return;
    }

    super.onKeyPressed(view, event);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
    if (prop == Cell.FOCUSED) {
      TextCell view = (TextCell) cell;
      PropertyChangeEvent<Boolean> event = (PropertyChangeEvent<Boolean>) e;

      view.caretVisible().set(event.getNewValue());
      if (!event.getNewValue()) {
        view.selectionVisible().set(false);
      }

      if (!event.getNewValue()) return;

      int caret = view.caretPosition().get();
      String text = view.text().get();
      if (text == null) text = "";

      if (!isFirstAllowed(view) && caret == 0) {
        view.caretPosition().set(1);
      }

      if (!isLastAllowed(view) && caret == text.length()) {
        view.caretPosition().set(text.length() - 1);
      }
    }

    if (prop == TextCell.TEXT) {
      TextCell view = (TextCell) cell;
      PropertyChangeEvent<String> event = (PropertyChangeEvent<String>) e;
      int newLength = event.getNewValue() != null ? event.getNewValue().length() : 0;
      if (view.caretPosition().get() > newLength) {
        view.caretPosition().set(newLength);
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
        TextCell popupView = CompletionSupport.showSideTransformPopup(cell, popup, completion.getItems());
        popupView.text().set(text);
        popupView.caretPosition().set(popupView.text().get().length());
        return true;
      }
    }
    return false;
  }

  @Override
  public void onMousePressed(Cell v, MouseEvent event) {
    TextCell view = (TextCell) v;
    if (!view.focusable().get()) return;
    int max = getMaxPos(view);
    int min = getMinPos(view);
    int offset = event.x() - view.origin().x;
    view.caretPosition().set(Math.max(min, Math.min(view.getCaretAt(offset), max)));
    view.focus();
    view.selectionVisible().set(false);
    event.consume();
  }
}