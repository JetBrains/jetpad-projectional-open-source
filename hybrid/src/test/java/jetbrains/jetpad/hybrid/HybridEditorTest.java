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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.hybrid.testapp.model.VarExpr;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class HybridEditorTest extends BaseTestCase {
  private ExprContainer container;
  private Registration registration;
  private ExprContainerMapper mapper;
  private HybridSynchronizer<Expr> sync;
  private Cell myTargetCell;

  @Before
  public void init() {
    CellContainer cc = new CellContainer();
    registration = RootController.install(cc);

    container = new ExprContainer();
    VarExpr expr = new VarExpr();
    expr.name.set("id");
    container.expr.set(expr);

    mapper = new ExprContainerMapper(container);
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cc.root.children().add(myTargetCell);
    sync = mapper.hybridSync;
  }

  @After
  public void dispose() {
    registration.remove();
    mapper.detachRoot();
  }

  @Test
  public void initial() {
    assertEquals(1, myTargetCell.children().size());
    assertEquals(Arrays.asList(new IdentifierToken("id")), sync.tokens());
  }
}
