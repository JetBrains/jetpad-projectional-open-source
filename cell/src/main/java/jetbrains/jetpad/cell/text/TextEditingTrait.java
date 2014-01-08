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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.TextClipboardContent;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

import java.util.List;

public class TextEditingTrait extends TextNavigationTrait {
  public TextEditingTrait() {
  }

  @Override
  public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
    if (spec == Completion.COMPLETION_CONTROLLER) {
      return getCompletionController((TextCell) cell);
    }

    return super.get(cell, spec);
  }

  private CompletionController getCompletionController(final TextCell view) {
    return new CompletionController() {
      @Override
      public boolean isActive() {
        return view.bottomPopup().get() != null;
      }

      @Override
      public boolean canActivate() {
        return !view.get(Completion.COMPLETION).get(new BaseCompletionParameters() {
          @Override
          public boolean isMenu() {
            return true;
          }
        }).isEmpty();
      }

      @Override
      public void setActive(boolean active) {
        if (active == isActive()) return;
        if (active) {
          CompletionSupport.showCompletion(view, view.get(Completion.COMPLETION).get(new BaseCompletionParameters() {
            @Override
            public boolean isMenu() {
              return true;
            }
          }), Registration.EMPTY, CellContainer.NULL_STATE);
        } else {
          view.get(CompletionSupport.HIDE_COMPLETION).run();
        }
      }

      @Override
      public boolean hasAmbiguousMatches() {
        String text = view.prefixText().get();
        CompletionHelper helper = CompletionHelper.completionFor(view, new BaseCompletionParameters() {
          @Override
          public boolean isMenu() {
            return true;
          }
        });
        return !helper.hasSingleMatch(text, false);
      }
    };
  }

  private String currentText(TextCell cell) {
    String currentText = cell.text().get();
    if (currentText == null) {
      currentText = "";
    }
    return currentText;
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextCell textCell = (TextCell) cell;
    String currentText = currentText(textCell);
    int caret = textCell.caretPosition().get();
    int textLen = currentText.length();

    if (textCell.selectionVisible().get() && (event.is(Key.BACKSPACE) || event.is(Key.DELETE))) {
      clearSelection(textCell);
      event.consume();
      return;
    }

    if (event.is(Key.BACKSPACE) && caret > 0) {
      textCell.caretPosition().set(textCell.caretPosition().get() - 1);
      setText(textCell, currentText.substring(0, caret - 1) + currentText.substring(caret));
      onAfterDelete(textCell);
      event.consume();
      return;
    }

    if (event.is(Key.DELETE) && caret < textLen) {
      setText(textCell, currentText.substring(0, caret) + currentText.substring(caret + 1));
      onAfterDelete(textCell);
      event.consume();
      return;
    }

    super.onKeyPressed(textCell, event);
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    TextCell textCell = (TextCell) cell;
    String currentText = currentText(textCell);
    CompletionController handler = getCompletionController(textCell);

    if (!(textCell.bottomPopup().get() != null)) {
      CompletionHelper completion = new CompletionHelper(textCell.get(Completion.COMPLETION).get(new BaseCompletionParameters() {
        @Override
        public boolean isMenu() {
          return true;
        }
      }));
      String prefixText = textCell.prefixText().get();
      if (textCell.isEnd()) {
        List<CompletionItem> matches = completion.matches(prefixText);
        List<CompletionItem> strictlyPrefixed = completion.strictlyPrefixedBy(prefixText);
        if (matches.size() == 1 && strictlyPrefixed.isEmpty()) {
          CompletionHelper rightTransform = CompletionHelper.rightTransformFor(textCell, new BaseCompletionParameters() {
            @Override
            public boolean isEndRightTransform() {
              return true;
            }

            @Override
            public boolean isMenu() {
              return true;
            }
          });
          if (!rightTransform.isEmpty() && textCell.get(TextEditing.DOT_LIKE_RT)) {
            if (!(textCell.rightPopup().get() != null)) {
              TextCell popup = CompletionSupport.showSideTransformPopup(textCell, textCell.rightPopup(), rightTransform.getItems());
              popup.get(Completion.COMPLETION_CONTROLLER).setActive(true);
            }
            event.consume();
            return;
          }
        }
      }

      List<CompletionItem> prefixed = completion.prefixedBy(prefixText);
      if (prefixed.size() == 1 && !currentText.isEmpty() && canCompleteWithCtrlSpace(textCell)) {
        prefixed.get(0).complete(prefixText).run();
        event.consume();
      } else if (handler.canActivate()) {
        handler.setActive(true);
        event.consume();
      }
      return;
    }


    super.onComplete(cell, event);
  }

  private void clearSelection(TextCell cell) {
    if (!cell.selectionVisible().get()) return;
    String text = cell.text().get();
    if (text == null) return;

    int caret = cell.caretPosition().get();
    int selStart = cell.selectionStart().get();

    int from = Math.min(selStart, caret);
    int to = Math.max(selStart, caret);

    cell.text().set(text.substring(0, from) + text.substring(to));
    cell.caretPosition().set(from);
    cell.selectionVisible().set(false);
  }

  protected boolean canCompleteWithCtrlSpace(TextCell text) {
    return true;
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    TextCell textCell = (TextCell) cell;
    clearSelection(textCell);

    String text = "" + event.keyChar();
    pasteText(textCell, text);
    onAfterType(textCell);
    event.consume();
  }

  @Override
  public void onPaste(Cell cell, PasteEvent event) {
    ClipboardContent content = event.getContent();
    if (!content.isSupported(ContentKinds.TEXT)) return;

    String text = content.get(ContentKinds.TEXT);

    StringBuilder newText = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) != '\n') {
        newText.append(text.charAt(i));
      }
    }

    TextCell textCell = (TextCell) cell;
    clearSelection(textCell);
    pasteText(textCell, newText.toString());
    event.consume();
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    onCopy(cell, event);
    clearSelection((TextCell) cell);
  }

  @Override
  public void onCopy(Cell cell, CopyCutEvent event) {
    TextCell textCell = (TextCell) cell;
    if (!textCell.selectionVisible().get()) return;
    int selStart = textCell.selectionStart().get();
    int caret = textCell.caretPosition().get();

    int from = Math.min(selStart, caret);
    int to = Math.max(selStart, caret);
    if (from == to) return;

    String selection = textCell.text().get().substring(from, to);
    event.consume(new TextClipboardContent(selection));
  }

  private void pasteText(TextCell cell, String text) {
    String currentText = cell.text().get();
    int caret = cell.caretPosition().get();
    if (currentText != null) {
      setText(cell, currentText.substring(0, caret) + text + currentText.substring(caret));
    } else {
      setText(cell, "" + text);
    }
    cell.caretPosition().set(caret + text.length());
  }

  protected void setText(TextCell cell, String text) {
    if (Strings.isNullOrEmpty(text)) {
      cell.dispatch(new Event(), Cells.BECAME_EMPTY);
    }

    cell.text().set(text);
  }

  protected boolean onAfterType(TextCell cell) {
    Supplier<Boolean> afterType = cell.get(TextEditing.AFTER_TYPE);
    if (afterType != null) {
      return afterType.get();
    }
    return false;
  }

  protected void onAfterDelete(TextCell textCell) {
  }
}