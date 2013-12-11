/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell.support;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.EditingTestCase;
import jetbrains.jetpad.projectional.cell.action.CellActions;
import jetbrains.jetpad.projectional.cell.testApp.mapper.BinExprCell;
import jetbrains.jetpad.projectional.cell.testApp.mapper.ExprMappers;
import jetbrains.jetpad.projectional.cell.testApp.model.ExprContainer;
import jetbrains.jetpad.projectional.cell.testApp.model.IdExpr;
import jetbrains.jetpad.projectional.cell.testApp.model.PlusExpr;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class DeleteActionsTest extends EditingTestCase {
  private ExprContainer container = new ExprContainer();
  private Mapper<?, ? extends Cell> rootMapper;
  private Mapper<?, BinExprCell> binExprMapper;
  private PlusExpr plusExpr;

  @Before
  public void initModel() {
    plusExpr = new PlusExpr();
    container.expr.set(plusExpr);
    rootMapper = ExprMappers.create(container);
    rootMapper.attachRoot();
    binExprMapper = (Mapper<?, BinExprCell>) rootMapper.getDescendantMapper(plusExpr);
    myCellContainer.root.children().add(rootMapper.getTarget());
  }

  @Test
  public void leftDelete() {
    IdExpr left = new IdExpr();
    plusExpr.left.set(left);
    CellActions.toFirstFocusable(binExprMapper.getTarget().right).execute();

    del();

    assertSame(left, container.expr.get());
  }

  @Test
  public void rightDelete() {
    IdExpr right = new IdExpr();
    plusExpr.right.set(right);
    CellActions.toFirstFocusable(binExprMapper.getTarget().left).execute();

    del();

    assertSame(right, container.expr.get());
  }
}