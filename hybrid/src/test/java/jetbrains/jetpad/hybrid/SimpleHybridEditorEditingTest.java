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

import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.IdExpr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleHybridEditorEditingTest extends BaseHybridEditorEditingTest<SimpleExprContainer, SimpleExprContainerMapper> {
  @Override
  protected SimpleExprContainer createContainer() {
    return new SimpleExprContainer();
  }

  @Override
  protected SimpleExprContainerMapper createMapper() {
    return new SimpleExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync() {
    return mapper.hybridSync;
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }

  @Override
  protected SimpleHybridEditorSpec<Expr> getSpec() {
    return mapper.hybridSyncSpec;
  }

  @Test
  public void typeDelete() {
    type("id");
    del();

    assertTrue(getExpr() instanceof IdExpr);
    assertEquals(1, targetCell.children().size());
    assertEquals(1, sync.tokens().size());
  }

  @Test
  public void typeBackspace() {
    type("id");
    type("+");
    backspace();
    backspace();

    assertTrue(getExpr() instanceof IdExpr);
    assertEquals(1, targetCell.children().size());
    assertEquals(1, sync.tokens().size());
  }

  @Test
  public void statePersistence() {
    assertNull(targetCell.get(CellStateHandler.PROPERTY));
  }
}
