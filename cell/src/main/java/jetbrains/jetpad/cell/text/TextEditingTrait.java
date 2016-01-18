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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.BaseCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.event.*;

import java.util.List;

public class TextEditingTrait extends TextNavigationTrait {
  public TextEditingTrait() {
  }

  @Override
  public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
    if (spec == Completion.COMPLETION_CONTROLLER) {
      return getCompletionController(TextEditing.cellTextEditor(cell));
    }

    return super.get(cell, spec);
  }

  private CompletionController getCompletionController(final CellTextEditor editor) {
    return new BaseCompletionController(editor.getCell()) {
      @Override
      public boolean canActivate() {
        BaseCompletionParameters params = new BaseCompletionParameters() {
          @Override
          public boolean isMenu() {
            return true;
          }
        };
        return !Completion.isCompletionEmpty(editor.getCell(), params);
      }

      @Override
      protected void doActivate(Runnable deactivate, Runnable restoreFocus) {
        CompletionSupport.showCompletion(editor, Completion.allCompletion(editor.getCell(), new BaseCompletionParameters() {
          @Override
          public boolean isMenu() {
            return true;
          }
        }), deactivate, restoreFocus);
      }

      @Override
      protected void doDeactivate() {
        editor.onCompletionHidden();
      }

      @Override
      public boolean hasAmbiguousMatches() {
        CompletionItems helper = Completion.completionFor(editor.getCell(), new BaseCompletionParameters() {
          @Override
          public boolean isMenu() {
            return true;
          }
        });
        return !helper.hasSingleMatch(TextEditing.getPrefixText(editor), false);
      }
    };
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    CellTextEditor editor = TextEditing.cellTextEditor(cell);
    String currentText = TextEditing.text(editor);
    int caret = editor.caretPosition().get();
    int textLen = currentText.length();

    if (editor.selectionVisible().get() && (event.is(Key.BACKSPACE) || event.is(Key.DELETE))) {
      clearSelection(editor);
      event.consume();
      return;
    }

    if (event.is(Key.BACKSPACE) && caret > 0) {
      editor.caretPosition().set(caret - 1);
      setText(editor, currentText.substring(0, caret - 1) + currentText.substring(caret));
      onAfterDelete(editor);
      event.consume();
      return;
    }

    if (event.is(Key.DELETE) && caret < textLen) {
      setText(editor, currentText.substring(0, caret) + currentText.substring(caret + 1));
      onAfterDelete(editor);
      event.consume();
      return;
    }

    if (event.is(KeyStrokeSpecs.DELETE_CURRENT) && cell.get(TextEditing.CLEAR_ON_DELETE) && !Strings.isNullOrEmpty(editor.text().get())) {
      setText(editor, "");
      onAfterDelete(editor);
      event.consume();
    }

    super.onKeyPressed(cell, event);
  }

  @Override
  public void onComplete(final Cell cell, CompletionEvent event) {
    CellTextEditor editor = TextEditing.cellTextEditor(cell);
    String currentText = TextEditing.text(editor);
    CompletionController handler = getCompletionController(editor);

    if (!handler.isActive()) {
      CompletionItems completion = new CompletionItems(cell.get(Completion.COMPLETION).get(CompletionParameters.EMPTY));
      String prefixText = TextEditing.getPrefixText(editor);
      if (TextEditing.isEnd(editor)) {
        List<CompletionItem> matches = completion.matches(prefixText);
        List<CompletionItem> strictlyPrefixed = completion.strictlyPrefixedBy(prefixText);
        if (matches.size() == 1 && strictlyPrefixed.isEmpty()) {
          BaseCompletionParameters cp = new BaseCompletionParameters() {
            @Override
            public boolean isEndRightTransform() {
              return true;
            }

            @Override
            public boolean isMenu() {
              return true;
            }
          };
          CompletionSupplier supplier = cell.get(Completion.RIGHT_TRANSFORM);
          if ((!supplier.isEmpty(cp) || !supplier.isAsyncEmpty(cp)) && cell.get(TextEditing.RT_ON_END)) {
            if (cell.get(Cell.RIGHT_POPUP) == null) {
              TextCell popup = CompletionSupport.showSideTransformPopup(cell, cell.rightPopup(), cell.get(Completion.RIGHT_TRANSFORM), true);
              popup.get(Completion.COMPLETION_CONTROLLER).activate(new Runnable() {
                @Override
                public void run() {
                  cell.focus();
                }
              });
            }
            event.consume();
            return;
          }
        }
      }

      List<CompletionItem> prefixed = completion.prefixedBy(prefixText);
      if (prefixed.size() == 1 && !currentText.isEmpty() && canCompleteWithCtrlSpace(editor)) {
        prefixed.get(0).complete(prefixText).run();
        event.consume();
      } else if (handler.canActivate()) {
        handler.activate();
        event.consume();
      }
      return;
    }


    super.onComplete(cell, event);
  }

  private void clearSelection(CellTextEditor editor) {
    if (!editor.selectionVisible().get()) return;
    String text = editor.text().get();
    if (text == null) return;

    int caret = editor.caretPosition().get();
    int selStart = editor.selectionStart().get();

    int from = Math.min(selStart, caret);
    int to = Math.max(selStart, caret);

    setText(editor, text.substring(0, from) + text.substring(to));
    editor.caretPosition().set(from);
    editor.selectionVisible().set(false);
  }

  protected boolean canCompleteWithCtrlSpace(CellTextEditor editor) {
    return true;
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    CellTextEditor editor = TextEditing.cellTextEditor(cell);
    clearSelection(editor);

    String text = "" + event.getKeyChar();
    pasteText(editor, text);
    editor.scrollToCaret();
    onAfterType(editor);
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

    CellTextEditor editor = TextEditing.cellTextEditor(cell);
    clearSelection(editor);
    pasteText(editor, newText.toString());
    event.consume();
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    onCopy(cell, event);
    clearSelection(TextEditing.cellTextEditor(cell));
  }

  @Override
  public void onCopy(Cell cell, CopyCutEvent event) {
    CellTextEditor editor = TextEditing.cellTextEditor(cell);
    if (!editor.selectionVisible().get()) return;

    int selStart = editor.selectionStart().get();
    int caret = editor.caretPosition().get();
    int from = Math.min(selStart, caret);
    int to = Math.max(selStart, caret);
    if (from == to) return;

    String selection = editor.text().get().substring(from, to);
    event.consume(new TextClipboardContent(selection));
  }

  private void pasteText(CellTextEditor editor, String text) {
    String currentText = editor.text().get();
    int caret = editor.caretPosition().get();
    if (currentText != null) {
      setText(editor, currentText.substring(0, caret) + text + currentText.substring(caret));
    } else {
      setText(editor, "" + text);
    }
    editor.caretPosition().set(caret + text.length());
  }

  protected void setText(CellTextEditor editor, String text) {
    if (Strings.isNullOrEmpty(text)) {
      editor.getCell().dispatch(new Event(), Cells.BECAME_EMPTY);
    }

    editor.text().set(text);
  }

  protected boolean onAfterType(TextEditor editor) {
    Supplier<Boolean> afterType = ((CellTextEditor) editor).getCell().get(TextEditing.AFTER_TYPE);
    if (afterType != null) {
      return afterType.get();
    }
    return false;
  }

  protected void onAfterDelete(CellTextEditor editor) {
  }
}