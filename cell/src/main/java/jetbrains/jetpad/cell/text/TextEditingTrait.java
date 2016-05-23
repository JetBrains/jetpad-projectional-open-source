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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.CellCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.BaseCompletionParameters;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.ClipboardContent;
import jetbrains.jetpad.event.ContentKinds;
import jetbrains.jetpad.event.CopyCutEvent;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.PasteEvent;
import jetbrains.jetpad.event.TextContentHelper;

import java.util.List;

public class TextEditingTrait extends TextNavigationTrait {
  public TextEditingTrait() {
  }

  @Override
  public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
    if (spec == Completion.COMPLETION_CONTROLLER) {
      return getCompletionController(cell);
    }
    if (spec == CompletionSupport.EDITOR) {
      return cell;
    }
    if (spec == TextEditing.EDITABLE) {
      return true;
    }
    return super.get(cell, spec);
  }

  private CompletionController getCompletionController(Cell cell) {
    return new CellCompletionController(cell);
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    TextCell editor = (TextCell) cell;
    String currentText = TextEditing.nonNullText(editor);
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
  public void onComplete(Cell cell, CompletionEvent event) {
    CompletionController handler = getCompletionController(cell);

    if (handler.isActive()) {
      super.onComplete(cell, event);
      return;
    }

    final TextCell editor = (TextCell) cell;

    CompletionItems completion = new CompletionItems(editor.get(Completion.COMPLETION).get(CompletionParameters.EMPTY));
    String prefixText = editor.getPrefixText();
    if (editor.isEnd() && editor.get(Completion.COMPLETION_CONFIG).canDoRightTransform(completion, prefixText)) {
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
      CompletionSupplier supplier = editor.get(Completion.RIGHT_TRANSFORM);
      if ((!supplier.isEmpty(cp) || !supplier.isAsyncEmpty(cp)) && editor.get(TextEditing.RT_ON_END)) {
        if (cell.get(Cell.RIGHT_POPUP) == null) {
          TextCell popup = CompletionSupport.showSideTransformPopup(editor, editor.rightPopup(), cell.get(Completion.RIGHT_TRANSFORM), true);
          popup.get(Completion.COMPLETION_CONTROLLER).activate(new Runnable() {
            @Override
            public void run() {
              editor.focus();
            }
          });
        }
        event.consume();
        return;
      }
    }

    List<CompletionItem> prefixed = completion.prefixedBy(prefixText);
    String currentText = TextEditing.nonNullText(editor);
    if (prefixed.size() == 1 && !currentText.isEmpty() && canCompleteWithCtrlSpace(editor)) {
      prefixed.get(0).complete(prefixText).run();
      cell.dispatch(new Event(), Cells.AFTER_COMPLETED);
      event.consume();
    } else if (handler.canActivate()) {
      handler.activate();
      event.consume();
    }
  }

  private void clearSelection(TextCell editor) {
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

  protected boolean canCompleteWithCtrlSpace(TextCell editor) {
    return true;
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    TextCell editor = (TextCell) cell;
    clearSelection(editor);

    String text = "" + event.getKeyChar();
    pasteText(editor, text);
    if (editor.isAttached()) {
      editor.scrollToCaret();
      onAfterType(editor);
    }
    event.consume();
  }

  @Override
  public void onPaste(Cell cell, PasteEvent event) {
    ClipboardContent content = event.getContent();
    if (!content.isSupported(ContentKinds.SINGLE_LINE_TEXT)) return;

    TextCell editor = (TextCell) cell;
    clearSelection(editor);
    pasteText(editor, content.get(ContentKinds.SINGLE_LINE_TEXT));
    if (editor.isAttached()) {
      onAfterPaste(editor);
    }
    event.consume();
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    onCopy(cell, event);
    clearSelection((TextCell) cell);
  }

  @Override
  public void onCopy(Cell cell, CopyCutEvent event) {
    TextCell editor = (TextCell) cell;
    if (!editor.selectionVisible().get()) return;

    int selStart = editor.selectionStart().get();
    int caret = editor.caretPosition().get();
    int from = Math.min(selStart, caret);
    int to = Math.max(selStart, caret);
    if (from == to) return;

    String selection = editor.text().get().substring(from, to);
    event.consume(TextContentHelper.createClipboardContent(selection));
  }

  private void pasteText(TextCell editor, String text) {
    String currentText = editor.text().get();
    int caret = editor.caretPosition().get();
    if (currentText != null) {
      setText(editor, currentText.substring(0, caret) + text + currentText.substring(caret));
    } else {
      setText(editor, "" + text);
    }
    if (editor.isAttached()) {
      editor.caretPosition().set(caret + text.length());
    }
  }

  protected void setText(TextCell editor, String text) {
    if (Strings.isNullOrEmpty(text)) {
      editor.dispatch(new Event(), Cells.BECAME_EMPTY);
    }
    editor.text().set(text);
  }

  protected boolean onAfterType(TextCell editor) {
    return callCustomProcessor(editor, TextEditing.AFTER_TYPE);
  }

  protected boolean onAfterPaste(TextCell editor) {
    return callCustomProcessor(editor, TextEditing.AFTER_PASTE);
  }

  private boolean callCustomProcessor(TextCell editor, CellTraitPropertySpec<Supplier<Boolean>> prop) {
    Supplier<Boolean> processor = editor.get(prop);
    if (processor != null) {
      return processor.get();
    }
    return false;
  }

  protected void onAfterDelete(TextCell editor) {
  }
}