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

import com.google.common.collect.Range;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.model.ComplexValueExpr;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.hybrid.testapp.model.ValueExpr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.util.RootController;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

import static jetbrains.jetpad.hybrid.SelectionPosition.*;
import static org.junit.Assert.*;

abstract class BaseHybridEditorEditingTest<MapperT extends Mapper<ExprContainer, ? extends Cell>> extends EditingTestCase {
  protected ExprContainer container = new ExprContainer();
  protected MapperT mapper = createMapper();
  protected BaseHybridSynchronizer<Expr, ?> sync;
  protected Cell targetCell;

  private Registration registration;

  protected abstract MapperT createMapper();
  protected abstract BaseHybridSynchronizer<Expr, ?> getSync(MapperT mapper);

  @Before
  public void init() {
    registration = RootController.install(myCellContainer);
    mapper.attachRoot();
    myCellContainer.root.children().add(targetCell = mapper.getTarget());
    CellActions.toFirstFocusable(mapper.getTarget()).run();
    sync = getSync(mapper);
  }

  @After
  public void dispose() {
    mapper.detachRoot();
    registration.remove();
  }

  protected ValueToken createComplexToken() {
    return new ValueToken(new ComplexValueExpr(), new ComplexValueCloner());
  }

  protected void assertTokens(Token... tokens) {
    assertEquals(Arrays.asList(tokens), sync.tokens());
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
