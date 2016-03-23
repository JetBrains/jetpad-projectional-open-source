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
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.hybrid.testapp.model.VarExpr;
import jetbrains.jetpad.projectional.cell.mapping.ToCellMapping;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HybridEditorTest extends BaseHybridEditorTest<ExprContainer, ExprContainerMapper> {
  @Override
  protected ExprContainer createContainer() {
    return new ExprContainer();
  }

  @Override
  protected ExprContainerMapper createMapper() {
    return new ExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(ExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }

  @Test
  public void initial() {
    container = createContainer();
    VarExpr expr = new VarExpr();
    expr.name.set("id");
    container.expr.set(expr);

    initEditor();

    assertEquals(1, myTargetCell.children().size());
    assertEquals(Arrays.asList(new IdentifierToken("id")), sync.tokens());
  }

  @Test
  public void testCellMapping() {
    initEditor();
    sync.tokens().addAll(Arrays.asList(Tokens.ID, Tokens.PLUS, Tokens.ID));

    ToCellMapping mapping = (ToCellMapping)sync;
    PlusExpr plus = (PlusExpr)getExpr();
    Assert.assertFalse(mapping.getCells(plus).isEmpty());
    Assert.assertFalse(mapping.getCells(plus.left.get()).isEmpty());
    Assert.assertFalse(mapping.getCells(plus.right.get()).isEmpty());

    for (Cell c : sync.tokenCells()) {
      Assert.assertNotNull(mapping.getSource(c));
    }
  }
}
