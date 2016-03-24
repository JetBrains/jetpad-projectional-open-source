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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.testapp.model.CommentToken;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.projectional.cell.mapping.ToCellMapping;
import org.junit.Test;

import static jetbrains.jetpad.hybrid.TokensUtil.integer;
import static org.junit.Assert.*;

public class HybridEditorEditingTest extends BaseHybridEditorEditingTest<ExprContainer, ExprContainerMapper> {
  @Override
  protected ExprContainer createContainer() {
    return new ExprContainer();
  }

  @Override
  protected ExprContainerMapper createMapper() {
    return new ExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync() {
    return mapper.hybridSync;
  }

  @Override
  protected SimpleHybridEditorSpec<Expr> getSpec() {
    return mapper.hybridSyncSpec.get();
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }

  @Test
  public void intermediateErrorState() {
    type("id+");

    assertTrue(getExpr() instanceof IdExpr);
    assertTokens(Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void deleteToEmpty() {
    container.expr.set(new IdExpr());
    setTokens(Tokens.PLUS);
    select(0, true);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertNull(container.expr.get());
    assertTrue(sync.placeholder().focused().get());
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
  public void tokenFocusAfterReparseAndSplitGivesDifferentTokens() {
    setTokens(new IdentifierToken("func("), Tokens.RP);

    select(0, false);
    left();
    type(" ");

    Cell focusedCell = sync.target().getContainer().focusedCell.get();
    assertTrue(((ToCellMapping)sync).getSource(focusedCell) instanceof CallExpr);
  }

  @Test
  public void valueTokenStatePersistence() {
    CellStateHandler handler = targetCell.get(CellStateHandler.PROPERTY);

    ValueExpr valExpr = new ValueExpr();
    setTokens(new ValueToken(valExpr, new ValueExprCloner()), Tokens.ID);

    CellState state = handler.saveState(targetCell);
    valExpr.val.set("z");

    handler.restoreState(targetCell, state);

    ValueToken newVal = (ValueToken) sync.tokens().get(0);
    assertNull(((ValueExpr) newVal.value()).val.get());
  }

  @Test
  public void valueTokenShouldBeFromModel() {
    ValueExpr expr = new ValueExpr();
    container.expr.set(expr);

    assertNotNull(mapper.getDescendantMapper(expr));
  }

  @Test
  public void dynamicSpec() {
    setTokens(Tokens.ID, Tokens.PLUS, Tokens.ID);
    assertTrue(container.expr.get() instanceof PlusExpr);

    mapper.hybridSyncSpec.set(new ExprHybridEditorSpec(Tokens.MUL, Tokens.PLUS));
    assertTrue(container.expr.get() instanceof MulExpr);
  }

  @Test
  public void commentInTheMiddle() {
    setTokens(integer(1), Tokens.PLUS, integer(2), Tokens.PLUS, integer(3));
    select(2, false);

    type("#");

    assertTokens(integer(1), Tokens.PLUS, integer(2), new CommentToken("#+3"));
  }

}
