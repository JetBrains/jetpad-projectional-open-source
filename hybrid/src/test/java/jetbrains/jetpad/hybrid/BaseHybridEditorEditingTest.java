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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.values.Color;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static jetbrains.jetpad.hybrid.SelectionPosition.*;
import static jetbrains.jetpad.hybrid.TokensUtil.*;
import static org.junit.Assert.*;

abstract class BaseHybridEditorEditingTest<ContainerT, MapperT extends Mapper<ContainerT, ? extends Cell>> extends EditingTestCase {
  protected ContainerT container;
  protected MapperT mapper;
  protected BaseHybridSynchronizer<Expr, ?> sync;
  protected Cell targetCell;

  private Registration registration;

  protected abstract ContainerT createContainer();
  protected abstract MapperT createMapper();
  protected abstract BaseHybridSynchronizer<Expr, ?> getSync();
  protected abstract Expr getExpr();
  protected abstract SimpleHybridEditorSpec<Expr> getSpec();

  @Before
  public void init() {
    container = createContainer();
    mapper = createMapper();
    registration = RootController.install(myCellContainer);
    mapper.attachRoot();
    myCellContainer.root.children().add(targetCell = mapper.getTarget());
    CellActions.toFirstFocusable(mapper.getTarget()).run();
    sync = getSync();
  }

  @After
  public void dispose() {
    mapper.detachRoot();
    registration.remove();
  }

  @Test
  public void simpleTyping() {
    type("id");
    type("+");
    type("id");

    assertTrue(getExpr() instanceof PlusExpr);
    assertEquals(5, targetCell.children().size());
    assertEquals(3, sync.tokens().size());
  }

  @Test
  public void errorState() {
    type("+");

    assertNull(getExpr());
    assertTokens(Tokens.PLUS);
  }

  @Test
  public void plusExpr() {
    type("id+id");

    assertTrue(getExpr() instanceof PlusExpr);
    assertEquals(5, targetCell.children().size());
  }

  @Test
  public void replacePlusWith() {
    setTokens(Tokens.PLUS);
    select(0, true);

    complete();
    type("i");
    enter();

    assertTrue(getExpr() instanceof IdExpr);
  }

  @Test
  public void leftTransformTyping() {
    setTokens(Tokens.PLUS, Tokens.ID);
    select(0, true);

    type("id");

    assertTrue(getExpr() instanceof PlusExpr);
  }

  @Test
  public void backspaceToEmpty() {
    type("id");

    press(Key.BACKSPACE);
    press(Key.BACKSPACE);

    assertNull(getExpr());
    assertTrue(sync.placeholder().focused().get());
  }

  @Test
  public void deleteEmptyToken() {
    setTokens(Tokens.MUL, Tokens.ID);
    select(1, false);

    backspace();
    backspace();
    backspace();

    assertTokens(Tokens.MUL);
    assertSelectedEnd(0);
  }

  @Test
  public void deleteLast() {
    setTokens(Tokens.MUL);
    select(0, false);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertTokens();
  }

  @Test
  public void backspaceInFirstPositionDoesNothing() {
    setTokens(Tokens.PLUS);
    select(0, true);

    backspace();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void deleteInlastPositionDoesNothing() {
    setTokens(Tokens.PLUS);
    select(0, false);

    del();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void tokenMergeWithDel() {
    setTokens(Tokens.PLUS, Tokens.PLUS);
    select(0, false);

    del();

    assertTokens(Tokens.INCREMENT);
  }

  @Test
  public void tokenMergeWithBackspace() {
    setTokens(Tokens.PLUS, Tokens.PLUS);
    select(1, true);

    backspace();

    assertTokens(Tokens.INCREMENT);
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals(1, (int) text.caretPosition().get());
  }

  @Test
  public void changedTokenWithBackspace() {
    setTokens(Tokens.INCREMENT);
    select(0, false);

    backspace();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void tokenMergeLeadingToErrorToken() {
    setTokens(Tokens.PLUS, Tokens.MUL);
    select(0, false);

    del();

    assertTokens(new ErrorToken("+*"));
  }

  @Test
  public void unselectableTokenNavigation() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, true);

    left();

    assertSelected(0);
  }

  @Test
  public void deleteDotWithBackspace() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, true);

    backspace();

    assertTokens(Tokens.ID, Tokens.ID);
  }

  @Test
  public void deleteDotWithDel() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(0, false);

    del();

    assertTokens(Tokens.ID, Tokens.ID);
  }

  @Test
  public void replaceVarWithId() {
    setTokens(new IdentifierToken("i"));
    select(0, false);

    type("d");

    assertTrue(getExpr() instanceof IdExpr);
  }

  @Test
  public void spaceLeadsToErrorToken() {
    setTokens(Tokens.PLUS);
    select(0, false);

    type(' ');

    assertTokens(Tokens.PLUS, new ErrorToken(""));
  }

  @Test
  public void backspaceAfterNoSpaceToRightEnablesAutoDelete() {
    setTokens(Tokens.LP, Tokens.ID);
    select(1, false);

    backspace();
    backspace();

    assertTokens(Tokens.LP);
  }

  @Test
  public void delBeforeNoSpaceToLeftEnablesAutoDeletion() {
    setTokens(Tokens.ID, Tokens.RP);
    select(0, true);

    del();
    del();

    assertTokens(Tokens.RP);
  }

  @Test
  public void multipleNoSpaceToLeftSelectedProperly() {
    setTokens(Tokens.RP, Tokens.RP, Tokens.RP, new IntValueToken(1));
    select(3, false);

    selectLeft(3);
    assertSelection(0, 4);
  }

  @Test
  public void noExtraPositionInParens() {
    setTokens(Tokens.LP, new IntValueToken(2), Tokens.RP);
    select(1, false);

    backspace();

    assertSelectedEnd(0);
    assertTokens(Tokens.LP, Tokens.RP);
  }

  @Test
  public void backspaceInSeqOfRP() {
    setTokens(Tokens.RP, Tokens.RP);
    select(1, false);

    backspace();

    assertSelectedEnd(0);
    assertTokens(Tokens.RP);
  }

  @Test
  public void delInSeqOnLP() {
    setTokens(Tokens.LP, Tokens.LP);
    select(0, true);

    del();

    assertSelected(0);
    assertTokens(Tokens.LP);
  }

  @Test
  public void splitTokenWithSpace() {
    setTokens(Tokens.INCREMENT);
    select(0, 1);

    type(' ');

    assertTokens(Tokens.PLUS, Tokens.PLUS);
  }

  @Test
  public void tokenSplitWithAnotherToken() {
    type("239");
    left();
    type("+");

    assertTokens(new IntValueToken(23), Tokens.PLUS, new IntValueToken(9));
    assertTrue(getExpr() instanceof PlusExpr);
  }

  @Test
  public void errorTokenMerge() {
    setTokens(new IntValueToken(44));
    select(0, true);
    right();

    type("  ");
    left();
    del();

    assertTokens(new IntValueToken(4), new IntValueToken(4));
  }

  @Test
  public void menuActivationDuringTyping() {
    setTokens(new IdentifierToken("i"));
    select(0, false);
    backspace();

    complete();
    type("+");

    assertTrue(isCompletionActive());
  }

  @Test
  public void menuDeactivationDuringTyping() {
    setTokens(Tokens.PLUS);
    select(0, false);
    complete();

    type("+");

    assertFalse(isCompletionActive());
  }

  @Test
  public void endRtWithActivatedMenu() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type("+");

    assertTokens(Tokens.DOT, Tokens.PLUS);
    assertTrue(isCompletionActive());
  }

  @Test
  public void endRtCompletionException() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    complete();

    assertTrue(isCompletionActive());
  }

  @Test
  public void endRtWhichLeadsToFocusAssertion() {
    setTokens(Tokens.ID, Tokens.DOT);
    select(1, false);

    complete();
    type("*");
    enter();

    assertTokens(Tokens.ID, Tokens.DOT, Tokens.MUL);
  }

  @Test
  public void endRtWithNoActivatedMenu() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type(")");

    assertTokens(Tokens.DOT, Tokens.RP);
    assertFalse(isCompletionActive());
  }


  @Test
  public void endRTWithAsyncCompletion() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type("async");
    enter();

    assertFalse(isCompletionActive());

    Token token = sync.tokens().get(1);
    assertTrue(token instanceof ValueToken && ((ValueToken) token).value() instanceof AsyncValueExpr);
  }


  @Test
  public void valueTokenParse() {
    type("value");

    assertTrue(sync.valid().get());
    assertTrue(getExpr() instanceof ValueExpr);
  }

  @Test
  public void valueTokenCompletion() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()));
    select(0, true);

    complete();
    type('+');
    enter();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void valueTokenLeftPrefix() {
    setTokens(new ValueToken(new ComplexValueExpr(), new ComplexValueCloner()));
    select(0, false);

    type("!");

    assertTrue(sync.tokens().size() == 2);
    assertTrue(sync.tokens().get(0) instanceof ValueToken);
    assertTrue(sync.tokens().get(1).equals(Tokens.FACTORIAL));
  }

  @Test
  public void valueTokenTransform() {
    type("value+value");

    assertTrue(sync.valid().get());
    assertTrue(getExpr() instanceof PlusExpr);
  }

  @Test
  public void complexValueTokenTransform() {
    setTokens(createComplexToken());
    select(0, false);

    type("+id");

    assertTrue(sync.valid().get());
    assertTrue(getExpr() instanceof PlusExpr);
    assertTrue(((PlusExpr) getExpr()).right.get() instanceof IdExpr);
  }

  @Test
  public void onCreateCalledForComplexToken() {
    type("aaaa");

    ComplexValueExpr expr = (ComplexValueExpr) ((ValueToken) sync.tokens().get(0)).value();
    Mapper<?, ? extends Cell> exprMapper = (Mapper<?, ? extends Cell>) mapper.getDescendantMapper(expr);
    HorizontalCell view = (HorizontalCell) exprMapper.getTarget();

    assertFocused(view.children().get(1));
  }

  @Test
  public void replaceValueTokens() {
    type("aaaa");

    select(0, true);
    complete();
    type("aaaa");

    assertEquals(1, sync.tokens().size());
  }

  @Test
  public void valueTokenDelete() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()), Tokens.RP);
    select(0, true);

    press(Key.BACKSPACE);

    assertTokens(Tokens.RP);
  }

  @Test
  public void selectUpFromBottomInCorrectOrder() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, false);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 3);
  }

  @Test
  public void selectUpFromComplexToken() {
    setTokens(createComplexToken());

    select(0, true);

    assertNoSelection();

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelected(0);
  }

  @Test
  public void selectDownFromComplexToken() {
    setTokens(createComplexToken());

    select(0, true);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelected(0);

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
  }


  @Test
  public void selectionDeleteWithDelDeleteAll() {
    setTokens(Tokens.LP, Tokens.RP);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens();
  }

  @Test
  public void selectionDeleteWithDelInTheMiddle() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens(Tokens.ID, Tokens.ID);
    assertSelected(0);
  }

  @Test
  public void selectionDeleteWithDelAtTheEnd() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(1, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens(Tokens.ID, Tokens.ID);
    assertSelected(1);
  }

  @Test
  public void selectionDeleteWithTyping() {
    setTokens(Tokens.ID, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    type("+");

    assertTokens(Tokens.PLUS);
    assertSelected(0);
  }

  @Test
  public void selectUpWithoutSelection() {
    setTokens(Tokens.ID, Tokens.PLUS, Tokens.ID);
    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 1);
  }

  @Test
  public void selectUpWithSelectionNoParse() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 3);
  }

  @Test
  public void selectUpDoesntConsumeInEmpty() {
    setTokens();
    sync.placeholder().focus();

    KeyEvent event = press(KeyStrokeSpecs.SELECT_UP);
    assertFalse(event.isConsumed());
  }

  @Test
  public void selectUpSelctionWithParse() {
    // TODO DP-920 failure!!!
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 2);
  }

  @Test
  public void selectDownDownWithParse() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);
    sync.select(Range.closed(0, 3));

    press(KeyStrokeSpecs.SELECT_DOWN);
    // TODO DP-920 failure!!!
    assertSelection(0, 2);
    assertSelected(0);
  }

  @Test
  public void selectUpInsideOfComplexValueToken() {
    ComplexValueExpr complexExpr = new ComplexValueExpr();
    setTokens(new ValueToken(complexExpr, new ComplexValueCloner()));

    Cell first = Composites.firstFocusable(sync.tokenCells().get(0));
    first.focus();

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 1);
  }

  @Test
  public void selectDownClearsSelection() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);
    sync.select(Range.closed(0, 1));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
    assertSelected(0);
  }

  @Test
  public void selectDownNoParse() {
    setTokens(Tokens.ID, Tokens.ID);
    select(0, true);

    sync.select(Range.closed(0, 2));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
    assertSelected(0);
  }

  @Test
  public void clearAll() {
    setTokens(Tokens.ID, Tokens.ID);

    select(0, true);

    press(Key.RIGHT, ModifierKey.SHIFT);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    assertTokens();

    assertFocused(sync.placeholder());
  }

  @Test
  public void clearFirstPart() {
    setTokens(Tokens.ID, Tokens.ID);

    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    Cell tokenCell = targetCell.children().get(0);
    assertFocused(tokenCell);
    assertTrue(Positions.isHomePosition(tokenCell));
    assertTokens(Tokens.ID);
  }

  @Test
  public void clearLastPart() {
    setTokens(Tokens.ID, Tokens.ID);

    select(1, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    Cell tokenCell = targetCell.children().get(0);
    assertFocused(tokenCell);
    assertTrue(Positions.isEndPosition(tokenCell));
    assertTokens(Tokens.ID);
  }

  @Test
  public void statePersistence() {
    CellStateHandler handler = targetCell.get(CellStateHandler.PROPERTY);

    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    CellState state = handler.saveState(targetCell);

    select(1, true);
    type(" id");

    handler.restoreState(targetCell, state);

    assertTokens(Tokens.ID, Tokens.ID, Tokens.ID);
  }

  @Test
  public void unselectableSelection() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertEquals(Range.closed(0, 2), sync.selection());
  }

  @Test
  public void caretPositionSavingOnCompletion() {
    setTokens(Tokens.PLUS);
    select(0, true);
    type("+");

    assertTokens(Tokens.INCREMENT);
    assertSelected(0);
    assertEquals(1, (int) ((TextCell) sync.tokenCells().get(0)).caretPosition().get());
  }

  @Test
  public void selectionIndex() {
    setTokens(Tokens.PLUS, Tokens.PLUS, Tokens.PLUS);
    select(1, false);

    Cell focusedCell = sync.target().getContainer().focusedCell.get();
    assertSame(sync.tokenCells().get(1), focusedCell);
  }

  @Test
  public void valueTokensShouldBeFromModelOnReparse() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()));

    assertNotNull(mapper.getDescendantMapper(getExpr()));
  }

  @Test
  public void toRightSimplePair() {
    setTokens(Tokens.LP, Tokens.RP);

    assertSame(tokenCell(1), sync.getPair((TextTokenCell) tokenCell(0)));
  }

  @Test
  public void toLeftSimplePair() {
    setTokens(Tokens.LP, Tokens.RP);

    assertSame(tokenCell(0), sync.getPair((TextTokenCell) tokenCell(1)));
  }

  @Test
  public void pairingIndexOutOfBoundsException() {
    setTokens(Tokens.LP, Tokens.RP);
    select(0, false);
    type("239");

    assertTokens(Tokens.LP, new IntValueToken(239), Tokens.RP);
  }

  @Test
  public void nestedPairing() {
    setTokens(Tokens.LP, Tokens.LP, Tokens.RP, Tokens.RP);

    assertSame(tokenCell(0), sync.getPair((TextTokenCell) tokenCell(3)));
  }

  @Test
  public void noPair() {
    setTokens(Tokens.LP);

    assertNull(sync.getPair((TextTokenCell) tokenCell(0)));
  }

  @Test
  public void additionalAsyncCompletion() {
    setTokens();
    select(0, true);

    complete();
    type("async");
    enter();

    assertSelected(0);

    Token token = sync.tokens().get(0);
    assertTrue(token instanceof ValueToken && ((ValueToken) token).value() instanceof AsyncValueExpr);
  }

  @Test
  public void typingWithCompletionMenuInPlaceholder() {
    setTokens();
    select(0, true);

    complete();
    type("+");

    assertTrue(isCompletionActive());
  }

  @Test
  public void emptyTokenCausesError() {
    setTokens(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));

    select(0, false);
    backspace();

    assertTrue(MessageController.hasError(sync.target()));
  }

  @Test
  public void hideTokensInMenu() {
    sync.setHideTokensInMenu(true);
    complete();
    type("aaa");

    assertEquals(0, sync.tokens().size());
  }

  @Test
  public void completeToEmptyString() {
    type("'");
    assertTokens(singleQtd(""));
  }

  @Test
  public void completeToNonemptyString() {
    type("abc'");
    home();
    type("'");
    assertTokens(singleQtd("abc"));
  }

  @Test
  public void typeQuoteBeforeNumber() {
    type("10");
    home();
    type("'");
    assertTokens(singleQtd(""), integer(10));
  }

  @Test
  public void copyPasteToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = copy();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void cutToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    assertTokens(Tokens.PLUS);

  }

  @Test
  public void cutPasteToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void pasteTokenToEmpty() {
    setTokens(Tokens.ID);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID);
  }

  @Test
  public void copySimpleExpr() {
    type("10+25");
    selectLeft(3);
    ClipboardContent clipboardContent = copy();
    assertEquals("10 + 25", clipboardContent.toString());
  }

  @Test
  public void pasteSimpleExprAsText() {
    paste("10+25");
    assertTokens(integer(10), Tokens.PLUS, integer(25));
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals("25", text.text().get());
    assertEquals(2, (int) text.caretPosition().get());
  }

  @Test
  public void pasteIncorrectText() {
    paste("10+bad");
    assertTokens(integer(10), Tokens.PLUS, error("bad"));
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals("bad", text.text().get());
    assertEquals(3, (int) text.caretPosition().get());
    assertEquals(Color.RED, text.textColor().get());
  }

  @Test
  public void pasteToNonempty() {
    type("id");
    paste("+ 10");
    assertTokens(Tokens.ID, Tokens.PLUS, integer(10));
  }

  @Test
  public void copyStringLiteralWithOtherTokens() {
    String text = "10+'text 1";   // Closing quote autocompletes
    type(text);
    right();
    selectLeft(text.length() + 1);
    ClipboardContent clipboardContent = copy();
    assertEquals("10 + 'text 1'", clipboardContent.toString());
  }

  @Test
  public void pasteStringLiteralWithOtherTokens() {
    paste("\"text 1\" + 10");
    assertTokensEqual(of(doubleQtd("text 1"), Tokens.PLUS, integer(10)), sync.tokens());
  }

  @Test
  public void editStringAfterCopyBeforePaste() {
    type("'x");
    right();
    selectLeft(1);

    ClipboardContent content = copy();
    String contentToString = content.toString();
    right();
    type("1");
    end(); end();

    paste(contentToString);

    assertTokens(singleQtd("1x"), singleQtd("x"));
  }

  @Test
  public void editStringAfterPaste() {
    type("'x");
    right();
    selectLeft(1);

    ClipboardContent content = copy();
    String contentToString = content.toString();
    end();

    paste(contentToString);
    paste(contentToString);

    left();
    type("2");
    left(5);
    type("1");

    assertTokens(singleQtd("x"), singleQtd("x1"), singleQtd("x2"));
  }

  @Test
  public void hideTokensInMenuForHybridWrapperRole() {
    HybridWrapperRoleCompletion<Object, Expr, Expr> hybridWrapperRole = new HybridWrapperRoleCompletion<>(getSpec(), null, null, true);
    CompletionSupplier roleCompletion = hybridWrapperRole.createRoleCompletion(mapper, null, null);
    CompletionParameters completionParameters = new BaseCompletionParameters() {
      @Override
      public boolean isMenu() {
        return true;
      }
    };
    Iterable<CompletionItem> completionItems = roleCompletion.get(completionParameters);
    assertTrue(FluentIterable.from(completionItems).isEmpty());
  }

  @Test
  public void bulkCompletionInHybridWrapperRole() {
    CompletionParameters requireBulkCompletion = new BaseCompletionParameters() {
      @Override
      public boolean isBulkCompletionRequired() {
        return true;
      }
    };
    CompletionSupplier roleCompletionSupplier = createHybridWrapperRoleCompletionSupplier();
    CompletionItems completionItems = new CompletionItems(roleCompletionSupplier.get(requireBulkCompletion));

    String code = "'text 1' + 1";
    for (boolean eagerCompletion : new boolean[] { false, true }) {
      assertTrue(completionItems.hasSingleMatch(code, eagerCompletion));
    }
    completionItems.completeFirstMatch(code);
    assertTokensEqual(of(singleQtd("text 1"), Tokens.PLUS, integer(1)), sync.tokens());
    assertFocused(targetCell.lastChild());
  }

  @Test
  public void hybridWrapperRoleCompletionRespectsPriorities() {
    CompletionSupplier roleCompletionSupplier = createHybridWrapperRoleCompletionSupplier();
    CompletionItems completionItems = new CompletionItems(roleCompletionSupplier.get(CompletionParameters.EMPTY));

    String code = "'";
    for (boolean eagerCompletion : new boolean[] { false, true }) {
      assertTrue(completionItems.hasSingleMatch(code, eagerCompletion));
    }
  }

  private CompletionSupplier createHybridWrapperRoleCompletionSupplier() {
    Supplier<ContainerT> containerSupplier = new Supplier<ContainerT>() {
      @Override
      public ContainerT get() {
        return container;
      }
    };
    Function<Mapper<?, ?>, BaseHybridSynchronizer<Expr, ?>> syncSupplier = new Function<Mapper<?, ?>, BaseHybridSynchronizer<Expr, ?>>() {
      @Override
      public BaseHybridSynchronizer<Expr, ?> apply(Mapper<?, ?> mapper) {
        return getSync();
      }
    };
    Role<ContainerT> containerRole = new Role<ContainerT>() {
      @Override
      public ContainerT get() {
        return container;
      }
      @Override
      public Runnable set(ContainerT target) {
        return Runnables.EMPTY;
      }
    };

    HybridWrapperRoleCompletion<Object, ContainerT, Expr> hybridWrapperRole = new HybridWrapperRoleCompletion<>(
        getSpec(), containerSupplier, syncSupplier);
    return hybridWrapperRole.createRoleCompletion(mapper, container, containerRole);
  }

  @Test
  public void processCommentsSideTransform() {
    setTokens(integer(1), Tokens.PLUS, integer(2), Tokens.PLUS, integer(3));
    select(2, false);

    type("#");

    assertTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", " + 3"));
    assertEquals(1, (int) ((TextCell) tokenCell(3)).caretPosition().get());
  }

  @Test
  public void processCommentsTokenCompletion() {
    setTokens(integer(1), Tokens.PLUS, integer(2), Tokens.PLUS, integer(3));
    select(3, true);

    type("#");

    assertTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", "+ 3"));
    assertEquals(1, (int) ((TextCell) tokenCell(3)).caretPosition().get());
  }

  @Test
  public void processCommentsAfterType() {
    setTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", " + 34"));
    select(3, false);

    left();
    type(" ");

    assertTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", " + 3 4"));
    assertEquals(6, (int) ((TextCell) tokenCell(3)).caretPosition().get());
  }

  @Test
  public void uncomment() {
    setTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", " + 3"));
    select(3, true);

    del();

    assertTokens(integer(1), Tokens.PLUS, integer(2), Tokens.PLUS, integer(3));
  }

  @Test
  public void uncommentWithInnerComment() {
    setTokens(integer(1), new CommentToken("#", " + 2 # + 3"));
    select(1, true);

    del();

    assertTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#", " + 3"));
  }

  @Test
  public void typeBeforeComment() {
    setTokens(new CommentToken("#", " + 3"));
    select(0, true);

    type("1");

    assertTokens(integer(1), new CommentToken("#", " + 3"));
  }

  @Test
  public void pasteBeforeComment() {
    setTokens(new CommentToken("#", "test"));
    select(0, true);

    paste("1");

    assertTokens(integer(1), new CommentToken("#", "test"));
  }

  @Test
  public void tokenUpdateAfterReparse() {
    setTokens(new IdentifierToken("x"), Tokens.LP);

    select(1, false);
    type(")");

    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void tokenUpdateAfterReparseAndSplit() {
    setTokens(new IdentifierToken("id*"), new IdentifierToken("x"), Tokens.LP, Tokens.RP);

    select(0, false);
    left();
    type(" ");

    assertTokens(Tokens.ID, Tokens.MUL, new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void reinitInTokensPostProcessor() {
    Synchronizer initialSync = sync;
    sync.setTokensEditPostProcessor(new TokensEditPostProcessor<Expr>() {
      @Override
      public void afterTokensEdit(List<Token> tokens, Expr value) {
        if (tokens.equals(Arrays.asList(integer(1), integer(2)))) {
          dispose();
          init();
        }
      }
    });
    type("1 2"); type("3");
    assertNotEquals(initialSync, sync);
    assertTokens(integer(3));
  }

  @Test
  public void postProcessTypingToEmpty() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type('1');
    assertTokensEqual(of(integer(1)), lastSeenTokens.get());
  }

  @Test
  public void postProcessTypingToNonEmpty() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1 2");
    assertTokensEqual(of(integer(1), integer(2)), lastSeenTokens.get());
  }

  @Test
  public void postProcessBackspace() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("12");
    backspace();
    assertTokensEqual(of(integer(1)), lastSeenTokens.get());
  }

  @Test
  public void postProcessDel() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("12");
    left(); del();
    assertTokensEqual(of(integer(1)), lastSeenTokens.get());
  }

  @Test
  public void noPostProcessingOnNavigationAndSelection() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1 2");
    left();
    selectLeft(1);
    assertTokensEqual(of(integer(1), integer(2)), lastSeenTokens.get());
  }

  @Test
  public void postProcessTokensPaste() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1 2");
    selectLeft(1);
    paste(copy().toString());
    assertTokensEqual(of(integer(1), integer(2), integer(2)), lastSeenTokens.get());
  }

  @Test
  public void postProcessTextPasteToEmpty() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    paste("1");
    assertTokensEqual(of(integer(1)), lastSeenTokens.get());
  }

  @Test
  public void postProcessTextPasteToNonEmpty() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1");
    paste("2");
    assertTokensEqual(of(integer(12)), lastSeenTokens.get());
  }

  @Test
  public void noPostProcessingOnEmptyPaste() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1");
    paste("");
    assertTokensEqual(of(integer(1)), lastSeenTokens.get());
  }

  @Test
  public void postProcessTokensCut() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1");
    selectLeft(1);
    cut();
    assertTrue(lastSeenTokens.get().isEmpty());
  }

  @Test
  public void postProcessTextCut() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1");

    TextTokenCell tokenCell = (TextTokenCell) sync.tokenCells().get(0);
    tokenCell.selectionStart().set(0);
    tokenCell.caretPosition().set(1);
    tokenCell.selectionVisible().set(true);
    cut();

    assertTrue(lastSeenTokens.get().isEmpty());
  }

  @Test
  public void postProcessAfterTokenAddition() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("'");
    assertTokensEqual(of(singleQtd("")), lastSeenTokens.get());
  }

  @Test
  public void postProcessorUninstall() {
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(true);
    type("1");
    backspace();
    sync.setTokensEditPostProcessor(null);
    type("2");
    assertTrue(lastSeenTokens.get().isEmpty());
  }

  @Test
  public void postProcessValueTokenRemoval() {
    // Such removes are processed with both onKeyPressed and
    // onKeyPressedLowPriority - and both may be passed the same tokens,
    // so we don't require tokens to be different on each handle.
    Value<List<Token>> lastSeenTokens = installTrackingPostProcessor(false);
    type("'");
    backspace();
    assertTrue(lastSeenTokens.get().isEmpty());
  }

  private Value<List<Token>> installTrackingPostProcessor(final boolean assertTokensChange) {
    final Value<List<Token>> lastSeenTokens = new Value<List<Token>>(Collections.EMPTY_LIST);
    sync.setTokensEditPostProcessor(new TokensEditPostProcessor<Expr>() {
      @Override
      public void afterTokensEdit(List<Token> tokens, Expr value) {
        if (assertTokensChange) {
          assertNotEquals(lastSeenTokens.get(), tokens);
        }
        lastSeenTokens.set(new ArrayList<>(tokens));
      }
    });
    return lastSeenTokens;
  }

  protected ValueToken createComplexToken() {
    return new ValueToken(new ComplexValueExpr(), new ComplexValueCloner());
  }

  protected void assertTokens(Token... tokens) {
    TokensUtil.assertTokensEqual(Arrays.asList(tokens), sync.tokens());
  }

  protected void setTokens(Token... tokens) {
    sync.setTokens(Arrays.asList(tokens));
  }

  protected void select(int index, boolean first) {
    sync.tokenOperations().select(index, first ? FIRST : LAST).run();
  }

  protected void select(int index, int pos) {
    sync.tokenOperations().select(index, pos).run();
  }

  protected Cell tokenCell(int index) {
    return sync.tokenCells().get(index);
  }

  protected Cell assertSelected(int index) {
    Cell cell = sync.tokenCells().get(index);
    assertTrue(cell.focused().get());
    return cell;
  }

  protected void assertSelectedEnd(int index) {
    Cell cell = assertSelected(index);
    assertTrue(Positions.isEndPosition(cell));
  }

  protected void assertSelection(int start, int end) {
    assertEquals(Range.closed(start, end), sync.selection());
  }

  protected void assertNoSelection() {
    assertFalse(sync.hasSelection());
  }

  protected boolean isCompletionActive() {
    Cell focused = myCellContainer.focusedCell.get();
    CompletionController ctrl = focused.get(Completion.COMPLETION_CONTROLLER);
    if (ctrl == null) return false;
    return ctrl.isActive();
  }

  protected void selectLeft(int steps) {
    for (int i = 0; i < steps; i++) {
      press(Key.LEFT, ModifierKey.SHIFT);
    }
  }

  protected static class ValueExprCloner implements ValueToken.ValueCloner<ValueExpr> {
    @Override
    public ValueExpr clone(ValueExpr val) {
      ValueExpr result = new ValueExpr();
      result.val.set(val.val.get());
      return result;
    }
  }

  protected static class ComplexValueCloner implements ValueToken.ValueCloner<ComplexValueExpr> {
    @Override
    public ComplexValueExpr clone(ComplexValueExpr val) {
      return new ComplexValueExpr();
    }
  }
}
