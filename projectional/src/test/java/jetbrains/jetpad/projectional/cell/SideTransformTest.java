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
package jetbrains.jetpad.projectional.cell;

import jetbrains.jetpad.base.edt.TestEventDispatchThread;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainerEdtUtil;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.testApp.mapper.ExprMappers;
import jetbrains.jetpad.projectional.testApp.model.AssignExpr;
import jetbrains.jetpad.projectional.testApp.model.EqExpr;
import jetbrains.jetpad.projectional.testApp.model.ExprContainer;
import jetbrains.jetpad.projectional.testApp.model.IdExpr;
import jetbrains.jetpad.projectional.testApp.model.PlusExpr;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SideTransformTest extends EditingTestCase {
  private TestEventDispatchThread edt = new TestEventDispatchThread();
  private ExprContainer container = new ExprContainer();
  private IdExpr rootId = new IdExpr();
  private Mapper<?, ? extends Cell> idMapper;
  private TextCell idView;

  @Before
  public void init() {
    container.expr.set(rootId);
    Mapper<?, ? extends Cell> rootMapper = ExprMappers.create(container);
    rootMapper.attachRoot();
    idMapper = (Mapper<?, ? extends Cell>) rootMapper.getDescendantMapper(rootId);
    myCellContainer.root.children().add(rootMapper.getTarget());
    idView = (TextCell) idMapper.getTarget();
    idView.focus();
    CellContainerEdtUtil.resetEdt(myCellContainer, edt);
  }

  @Override
  protected void type(String s) {
    super.type(s);
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
  }

  @Test
  public void simpleRightTransform() {
    idView.caretPosition().set(2);

    type("+");

    assertTrue(container.expr.get() instanceof PlusExpr);
    PlusExpr plusExpr = (PlusExpr) container.expr.get();
    assertSame(idMapper.getSource(), plusExpr.left.get());
  }

  @Test
  public void dismissSideTransform() {
    idView.caretPosition().set(2);
    type(' ');
    escape();

    assertFocused(idView);
  }

  @Test
  public void rightTransformWithSpace() {
    idView.caretPosition().set(2);

    type(" ");
    type("+");
    complete();

    assertTrue(container.expr.get() instanceof PlusExpr);
    PlusExpr plusExpr = (PlusExpr) container.expr.get();
    assertSame(idMapper.getSource(), plusExpr.left.get());
  }

  @Test
  public void simpleLeftTransform() {
    idView.caretPosition().set(0);

    type("+");

    assertTrue(container.expr.get() instanceof PlusExpr);
    PlusExpr plusExpr = (PlusExpr) container.expr.get();
    assertSame(idMapper.getSource(), plusExpr.right.get());
  }

  @Test
  public void leftTransformWithSpace() {
    idView.caretPosition().set(0);

    type(" ");
    type("+");
    complete();

    assertTrue(container.expr.get() instanceof PlusExpr);
    PlusExpr plusExpr = (PlusExpr) container.expr.get();
    assertSame(idMapper.getSource(), plusExpr.right.get());
  }

  @Test
  public void rightTransformAutoComplete() {
    idView.caretPosition().set(2);
    autoComplete(true);
  }

  @Test
  public void leftTransformAutoComplete() {
    idView.caretPosition().set(0);
    autoComplete(false);
  }

  private void autoComplete(boolean rightTransform) {
    type(" ");
    type("+");
    type("i");

    assertTrue(container.expr.get() instanceof PlusExpr);
    PlusExpr plusExpr = (PlusExpr) container.expr.get();
    if (rightTransform) {
      assertSame(idMapper.getSource(), plusExpr.left.get());
    } else {
      assertSame(idMapper.getSource(), plusExpr.right.get());
    }
  }

  @Test
  public void leftTransformAmbiguity() {
    idView.caretPosition().set(0);
    transformAmbiguity();
  }

  @Test
  public void rightTransformAmbiguity() {
    idView.caretPosition().set(2);
    transformAmbiguity();
  }

  private void transformAmbiguity() {
    type("=");
    type("=");
    type("i");
    assertTrue(container.expr.get() instanceof EqExpr);
  }

  @Test
  public void rightTransformAmbiguityResolveInCaseOfNoMatches() {
    idView.caretPosition().set(2);
    type("=");
    type("i");
    assertTrue(container.expr.get() instanceof AssignExpr);
  }

  @Test
  public void rightTransformResolvesCompletionAmbiguity() {
    backspace();
    type("+");

    assertTrue(container.expr.get() instanceof PlusExpr);
  }
}