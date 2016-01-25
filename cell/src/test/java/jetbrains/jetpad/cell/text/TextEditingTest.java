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

import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TextEditingTest extends EditingTestCase {
  private TextCell textView = new TextCell();
  private TextCell navTextView1 = new TextCell();
  private TextCell navTextView2 = new TextCell();

  private String completed;

  @Before
  public void init() {
    textView.focusable().set(true);

    myCellContainer.root.children().addAll(Arrays.asList(textView, navTextView1, navTextView2));

    textView.text().set("abc");
    navTextView1.text().set("abc");
    navTextView2.text().set("abc");

    textView.addTrait(TextEditing.textEditing());
    navTextView1.addTrait(TextEditing.textNavigation(false, true));
    navTextView2.addTrait(TextEditing.textNavigation(true, false));

    textView.focus();
  }

  @Test
  public void focusMakesCaretVisible() {
    myCellContainer.focusedCell.set(null);

    assertFalse(textView.caretVisible().get());

    textView.focus();

    assertTrue(textView.caretVisible().get());
  }

  @Test
  public void textChangeUpdatesCaretPosition() {
    textView.caretPosition().set(3);
    textView.text().set("a");

    assertCaret(1);
  }

  @Test
  public void leftWorks() {
    textView.caretPosition().set(3);

    left();

    assertCaret(2);
  }

  @Test
  public void rightWorks() {
    textView.caretPosition().set(2);

    right();

    assertCaret(3);
  }

  private void assertFirstPosActionWorks(Runnable action) {
    textView.caretPosition().set(3);
    action.run();
    assertCaret(0);
  }

  @Test
  public void altLeftWorks() {
    assertFirstPosActionWorks(new Runnable() {
      @Override
      public void run() {
        altLeft();
      }
    });
  }

  @Test
  public void homeWorks() {
    assertFirstPosActionWorks(new Runnable() {
      @Override
      public void run() {
        home();
      }
    });
  }

  private void assertLastPosActionWorks(Runnable action) {
    textView.caretPosition().set(2);
    action.run();
    assertCaret(3);
  }

  @Test
  public void altRightWorks() {
    assertLastPosActionWorks(new Runnable() {
      @Override
      public void run() {
        altRight();
      }
    });
  }

  @Test
  public void endWorks() {
    assertLastPosActionWorks(new Runnable() {
      @Override
      public void run() {
        end();
      }
    });
  }

  @Test
  public void backspaceWorks() {
    textView.caretPosition().set(1);

    backspace();

    assertCaret(0);
    assertText("bc");
  }

  @Test
  public void deleteWorks() {
    textView.caretPosition().set(0);

    del();

    assertCaret(0);
    assertText("bc");
  }

  @Test
  public void typingWorks() {
    textView.caretPosition().set(1);

    type('z');

    assertCaret(2);
    assertText("azbc");
  }

  @Test
  public void navigateInNullText() {
    textView.text().set(null);

    left();

    assertText(null);
    assertCaret(0);
  }

  @Test
  public void typeInNullText() {
    textView.text().set(null);

    type('z');

    assertCaret(1);
    assertText("z");
  }

  @Test
  public void focusIntoNoFirstPos() {
    navTextView1.focus();

    assertEquals((Integer) 1, navTextView1.caretPosition().get());
  }

  @Test
  public void focusIntoNoLastPos() {
    navTextView2.caretPosition().set(3);
    navTextView2.focus();

    assertEquals((Integer) 2, navTextView2.caretPosition().get());
  }

  @Test
  public void cantGoToFirstPosIfDisabled() {
    navTextView1.caretPosition().set(1);

    left();

    assertEquals((Integer) 1, navTextView1.caretPosition().get());
  }

  @Test
  public void cantGoToLastPosIfDisabled() {
    navTextView1.caretPosition().set(2);

    right();

    assertEquals((Integer) 2, navTextView1.caretPosition().get());
  }

  @Test
  public void homeWorksIfFirstDisabled() {
    navTextView1.caretPosition().set(2);
    navTextView1.get(PositionHandler.PROPERTY).home();

    assertEquals((Integer) 1, navTextView1.caretPosition().get());
  }

  @Test
  public void endWorksIfFirstDisabled() {
    navTextView2.caretPosition().set(0);
    navTextView2.get(PositionHandler.PROPERTY).end();

    assertEquals((Integer) 2, navTextView2.caretPosition().get());
  }

  @Test
  public void rtInNavigationItemsInCaseOfAmbiguity() {
    createTestCell(false, 2);

    type("x");
    assertNull(completed);
    complete();
    enter();

    assertEquals("x", completed);
  }

  @Test
  public void ltInNavigationItemsInCaseOfAmbiguity() {
    createTestCell(true, 2);

    type("x");
    assertNull(completed);
    complete();
    enter();

    assertEquals("x", completed);
  }

  @Test
  public void rtInNavigationItemsInCaseOfNoAmbiguity() {
    createTestCell(false, 1);

    type("x");

    assertEquals("x", completed);
  }

  @Test
  public void ltInNavigationItemsInCaseOfNoAmbiguity() {
    createTestCell(true, 1);

    type("x");

    assertEquals("x", completed);
  }

  @Test
  public void rtInNavigationItemsInCaseOfLongText() {
    createTestCell(false, 1);

    type("yy");

    assertEquals("yy", completed);
  }

  @Test
  public void ltInNavigationItemsInCaseOfLongText() {
    createTestCell(true, 1);

    type("yy");

    assertEquals("yy", completed);
  }

  @Test
  public void rtTypeAndBackspaceReturnsFocus() {
    TextCell cell = createTestCell(true, 2);

    type("x");
    backspace();

    assertTrue(cell.focused().get());
  }

  @Test
  public void ltTypeAndBackspaceReturnsFocus() {
    TextCell cell = createTestCell(false, 2);

    type("x");
    backspace();

    assertTrue(cell.focused().get());
  }

  @Test
  public void completionOnEndInvokesRtCompletion() {
    createTestCell(false, 1);

    complete();
    enter();

    assertEquals("x", completed);
  }

  @Test
  public void completionOnHomeInvokesLtCompletion() {
    createTestCell(true, 1);

    complete();
    enter();

    assertEquals("x", completed);
  }

  @Test
  public void completionOnEndIsCancellableWithEsc() {
    TextCell cell = createTestCell(false, 1);

    complete();
    escape();

    assertNull(completed);
    assertTrue(cell.focused().get());
  }

  @Test
  public void completionOnHomeIsCancellableWithEsc() {
    TextCell cell = createTestCell(true, 1);

    complete();
    escape();

    assertNull(completed);
    assertTrue(cell.focused().get());
  }


  @Test
  public void movementPlusShiftStartsSelection() {
    press(Key.RIGHT, ModifierKey.SHIFT);

    assertSelection(true);
    assertSelectionStart(0);
    assertCaret(1);
  }

  @Test
  public void movementPlusShiftExpandsSelection() {
    textView.caretPosition().set(1);
    textView.selectionStart().set(0);
    textView.selectionVisible().set(true);

    press(Key.RIGHT, ModifierKey.SHIFT);

    assertSelection(true);
    assertSelectionStart(0);
    assertCaret(2);
  }

  @Test
  public void movementWithoutShiftStopsSelection() {
    textView.caretPosition().set(1);
    textView.selectionStart().set(0);
    textView.selectionVisible().set(true);

    right();

    assertFalse(textView.selectionVisible().get());
    assertCaret(2);
  }


  @Test
  public void selectionWithCtrlShiftRight() {
    textView.caretPosition().set(0);

    press(Key.RIGHT, ModifierKey.CONTROL, ModifierKey.SHIFT);

    assertTrue(textView.selectionVisible().get());
    assertCaret(textView.text().get().length());
  }

  @Test
  public void selectAll() {
    press(Key.A, ModifierKey.CONTROL);

    assertSelection(true);
    assertSelectionStart(0);
    assertCaret(3);
  }

  @Test
  public void deleteRemovesSelection() {
    press(Key.RIGHT, ModifierKey.SHIFT);
    press(Key.RIGHT, ModifierKey.SHIFT);
    del();

    assertSelection(false);
    assertCaret(0);
    assertEquals("c", textView.text().get());
  }

  @Test
  public void typingRemovesSelection() {
    press(Key.RIGHT, ModifierKey.SHIFT);
    press(Key.RIGHT, ModifierKey.SHIFT);

    type("z");

    assertSelection(false);
    assertCaret(1);
    assertEquals("zc", textView.text().get());
  }

  @Test
  public void mousePressResetsSelection() {
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertTrue(textView.selectionVisible().get());

    mousePress(textView.getBounds().center());

    assertFalse(textView.selectionVisible().get());
  }

  @Test
  public void mouseDragSelects() {
    Rectangle bounds = textView.getBounds();
    mousePress(bounds.origin);
    mouseDrag(bounds.origin.add(new Vector(bounds.dimension.x, 0)));

    assertTrue(textView.selectionVisible().get());
    assertEquals(0, (int) textView.caretPosition().get());
    assertEquals(3, (int) textView.selectionStart().get());
  }

  @Test
  public void nonConsumedShiftShortcutShouldntLeadToSelection() {
    press(Key.A, ModifierKey.SHIFT);

    assertSelection(false);
  }

  @Test
  public void nullTextTypingNPE() {
    textView.text().set(null);

    press(Key.A, ModifierKey.SHIFT);
    type("z");

    assertEquals("z", textView.text().get());
  }


  @Test
  public void textClear() {
    textView.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.CLEAR_ON_DELETE) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    textView.text().set("TestText");
    press(KeyStrokeSpecs.DELETE_CURRENT);

    assertEquals("", textView.text().get());
  }

  @Test
  public void becameEmptyFiredForClearSelection() {
    final Value<Boolean> becameEmptyFired = new Value<>(false);
    textView.addTrait(new CellTrait() {
      @Override
      public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
        if (spec == Cells.BECAME_EMPTY) {
          becameEmptyFired.set(true);
          event.consume();
          return;
        }
        super.onCellTraitEvent(cell, spec, event);
      }
    });

    textView.text().set("TestText");
    textView.caretPosition().set(8);
    textView.selectionStart().set(0);
    textView.selectionVisible().set(true);
    press(Key.BACKSPACE);

    assertEquals("", textView.text().get());
    assertTrue(becameEmptyFired.get());
  }

  @Test
  public void textClearOnEmpty() {
    textView.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.CLEAR_ON_DELETE) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    textView.text().set("");
    KeyEvent event = press(KeyStrokeSpecs.DELETE_CURRENT);

    assertFalse(event.isConsumed());
    assertEquals("", textView.text().get());
  }

  @Test
  public void textPaste() {
    textView.text().set("xy");
    textView.caretPosition().set(1);

    paste("abc");

    assertEquals("xabcy", textView.text().get());
    assertEquals(4, (int) textView.caretPosition().get());
  }

  @Test
  public void textPasteFiltersNewLines() {
    textView.text().set("");
    textView.caretPosition().set(0);

    paste("\na");

    assertEquals("a", textView.text().get());
  }

  @Test
  public void textCopy() {
    textView.text().set("xyz");
    textView.caretPosition().set(1);
    textView.selectionStart().set(2);
    textView.selectionVisible().set(true);

    CopyCutEvent event = new CopyCutEvent(false);
    myCellContainer.copy(event);

    ClipboardContent result = event.getResult();
    assertTrue(event.isConsumed());
    assertTrue(result.isSupported(ContentKinds.TEXT));
    assertEquals("y", result.get(ContentKinds.TEXT));
  }

  @Test
  public void textCut() {
    textView.text().set("xyz");
    textView.caretPosition().set(1);
    textView.selectionStart().set(2);
    textView.selectionVisible().set(true);

    CopyCutEvent event = new CopyCutEvent(true);
    myCellContainer.cut(event);
    ClipboardContent result = event.getResult();

    assertTrue(event.isConsumed());
    assertTrue(result.isSupported(ContentKinds.TEXT));
    assertEquals("y", result.get(ContentKinds.TEXT));
    assertEquals("xz", textView.text().get());
    assertEquals(1, (int) textView.caretPosition().get());
    assertFalse(textView.selectionVisible().get());
  }

  private TextCell createTestCell(final boolean left, final int count) {
    final TextCell navText = new TextCell();

    navText.text().set("x");
    navText.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if ((spec == Completion.LEFT_TRANSFORM && left) || (spec == Completion.RIGHT_TRANSFORM && !left)) {
          return createTestCompletion(count);
        }

        return super.get(cell, spec);
      }
    });
    navText.addTrait(TextEditing.textNavigation(true, true));

    myCellContainer.root.children().add(navText);
    navText.focus();
    navText.caretPosition().set(left ? 0 : 1);

    return navText;
  }

  private void assertCaret(int caret) {
    assertEquals(caret, (int) textView.caretPosition().get());
  }

  private void assertSelection(boolean active) {
    assertEquals(active, textView.selectionVisible().get());
  }

  private void assertSelectionStart(int selection) {
    assertEquals(selection, (int) textView.selectionStart().get());
  }

  private void assertText(String text) {
    assertEquals(text, textView.text().get());
  }

  private CompletionSupplier createTestCompletion(final int xCount) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        for (int i = 0; i < xCount; i++) {
          result.add(createCompletionItem("x"));
        }
        result.add(createCompletionItem("yy"));
        return result;
      }
    };
  }

  private SimpleCompletionItem createCompletionItem(final String visibleText) {
    return new SimpleCompletionItem(visibleText) {
      @Override
      public Runnable complete(String text) {
        completed = visibleText;
        return Runnables.EMPTY;
      }
    };
  }
}