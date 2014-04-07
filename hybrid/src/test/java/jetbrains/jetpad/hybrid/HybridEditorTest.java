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
package jetbrains.jetpad.hybrid;

import com.google.common.collect.Range;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.mapper.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;
import static org.junit.Assert.*;

public class HybridEditorTest extends EditingTestCase {
  private ExprContainer container = new ExprContainer();
  private ExprContainerMapper mapper = new ExprContainerMapper(container);
  private HybridSynchronizer<Expr> sync;
  private Cell myTargetCell;

  @Before
  public void init() {
    RootController.install(myCellContainer);
    mapper.attachRoot();
    myCellContainer.root.children().add(myTargetCell = mapper.getTarget());
    CellActions.toFirstFocusable(mapper.getTarget()).run();
    sync = mapper.hybridSync;
  }

  @Test
  public void simpleTyping() {
    type("id");

    assertTrue(container.expr.get() instanceof IdExpr);
  }

  @Test
  public void errorState() {
    type("+");

    Assert.assertNull(container.expr.get());
    assertTokens(Tokens.PLUS);
  }

  @Test
  public void plusExpr() {
    type("id+id");

    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void intermediateErrorState() {
    type("id+");

    assertTrue(container.expr.get() instanceof IdExpr);
    assertTokens(Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void replacePlusWith() {
    setTokens(Tokens.PLUS);
    select(0, true);

    complete();
    type("i");
    enter();

    assertTrue(container.expr.get() instanceof IdExpr);
  }

  @Test
  public void leftTransformTyping() {
    setTokens(Tokens.PLUS, Tokens.ID);
    select(0, true);

    type("id");

    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void deleteToEmpty() {
    container.expr.set(new IdExpr());
    setTokens(Tokens.PLUS);
    select(0, true);

    press(Key.DELETE, ModifierKey.CONTROL);

    Assert.assertNull(container.expr.get());
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

    assertTrue(container.expr.get() instanceof IdExpr);
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
    assertTrue(container.expr.get() instanceof PlusExpr);
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
    down();
    down();
    down();
    enter();

    assertTokens(Tokens.ID, Tokens.DOT, Tokens.PLUS);
  }

  @Test
  public void endRtWithNoActivedMenu() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type(")");

    assertTokens(Tokens.DOT, Tokens.RP);
    assertFalse(isCompletionActive());
  }


  @Test
  public void valueTokenParse() {
    type("value");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof ValueExpr);
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
  public void valueTokenTransform() {
    type("value+value");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void complexValueTokenTransform() {
    setTokens(createComplexToken());
    select(0, false);

    type("+id");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof PlusExpr);
    assertTrue(((PlusExpr) container.expr.get()).right.get() instanceof IdExpr);
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
  public void valueTokenDelete() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()), Tokens.RP);
    select(0, true);

    press(Key.BACKSPACE);

    assertTokens(Tokens.RP);
  }

  @Test
  public void tokenUpdateAfterReparse() {
    setTokens(new IdentifierToken("x"), Tokens.LP);

    select(1, false);
    type(")");

    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
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

    KeyEvent event = press(KeyStrokeSpecs.SELECT_UP);;
    assertFalse(event.isConsumed());
  }

  @Test
  public void selectUpSelctionWithParse() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 2);
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
  public void selectDownDownWithParse() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);
    sync.select(Range.closed(0, 3));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertSelection(0, 2);
    assertSelected(0);
  }

  @Test
  public void copyPaste() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);

    press(Key.RIGHT, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.COPY);
    press(KeyStrokeSpecs.PASTE);

    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void cut() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);

    press(Key.RIGHT, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.CUT);

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void cutPaste() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);

    press(Key.RIGHT, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.CUT);
    press(KeyStrokeSpecs.PASTE);
    press(KeyStrokeSpecs.PASTE);

    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
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

    Cell tokenCell = myTargetCell.children().get(0);
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

    Cell tokenCell = myTargetCell.children().get(0);
    assertFocused(tokenCell);
    assertTrue(Positions.isEndPosition(tokenCell));
    assertTokens(Tokens.ID);
  }

  @Test
  public void statePersistence() {
    CellStateHandler handler = myTargetCell.get(CellStateHandler.PROPERTY);

    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    Object state = handler.saveState(myTargetCell);

    select(1, true);
    type(" id");

    handler.restoreState(myTargetCell, state);

    assertTokens(Tokens.ID, Tokens.ID, Tokens.ID);
  }

  @Test
  public void valueTokenStatePersistence() {
    CellStateHandler handler = myTargetCell.get(CellStateHandler.PROPERTY);

    ValueExpr valExpr = new ValueExpr();
    setTokens(new ValueToken(valExpr, new ValueExprCloner()), Tokens.ID);

    Object state = handler.saveState(myTargetCell);
    valExpr.val.set("z");

    handler.restoreState(myTargetCell, state);

    ValueToken newVal = (ValueToken) sync.tokens().get(0);
    assertNull(((ValueExpr) newVal.value()).val.get());
  }

  @Test
  public void unselectableSelection() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    junit.framework.Assert.assertEquals(Range.closed(0, 2), sync.selection());
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

    assertEquals(1, sync.focusedIndex());
  }

  @Test
  public void valueTokenShouldBeFromModel() {
    ValueExpr expr = new ValueExpr();
    container.expr.set(expr);

    assertNotNull(mapper.getDescendantMapper(expr));
  }

  @Test
  public void valueTokensShouldBeFromModelOnReparse() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()));

    Expr expr = container.expr.get();
    assertNotNull(mapper.getDescendantMapper(expr));
  }

  private ValueToken createComplexToken() {
    return new ValueToken(new ComplexValueExpr(), new ComplexValueCloner());
  }

  private void assertTokens(Token... tokens) {
    assertEquals(Arrays.asList(tokens), sync.tokens());
  }

  private void setTokens(Token... tokens) {
    sync.tokens().clear();
    sync.tokens().addAll(Arrays.asList(tokens));
    sync.tokenListEditor().updateToPrintedTokens();
  }

  private void select(int index, boolean first) {
    sync.tokenOperations().select(index, first ? FIRST : LAST).run();
  }

  private void select(int index, int pos) {
    sync.tokenOperations().select(index, pos).run();
  }

  private Cell assertSelected(int index) {
    Cell cell = sync.tokenCells().get(index);
    assertTrue(cell.focused().get());
    return cell;
  }

  private void assertSelectedEnd(int index) {
    Cell cell = assertSelected(index);
    assertTrue(Positions.isEndPosition(cell));
  }

  private void assertSelection(int start, int end) {
    assertEquals(Range.closed(start, end), sync.selection());
  }

  private void assertNoSelection() {
    assertFalse(sync.hasSelection());
  }

  private boolean isCompletionActive() {
    Cell focused = myCellContainer.focusedCell.get();
    CompletionController ctrl = focused.get(Completion.COMPLETION_CONTROLLER);
    if (ctrl == null) return false;
    return ctrl.isActive();
  }

  private static class ValueExprCloner implements ValueToken.ValueCloner<ValueExpr> {
    @Override
    public ValueExpr clone(ValueExpr val) {
      ValueExpr result = new ValueExpr();
      result.val.set(val.val.get());
      return result;
    }
  }

  private static class ComplexValueCloner implements ValueToken.ValueCloner<ComplexValueExpr> {
    @Override
    public ComplexValueExpr clone(ComplexValueExpr val) {
      return new ComplexValueExpr();
    }
  }
}