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
package jetbrains.jetpad.projectional.binOp;

import jetbrains.jetpad.projectional.testApp.model.AssignExpr;
import jetbrains.jetpad.projectional.testApp.model.BinExpr;
import jetbrains.jetpad.projectional.testApp.model.ExprContainer;
import jetbrains.jetpad.projectional.testApp.model.PlusExpr;
import org.junit.Test;

import static jetbrains.jetpad.projectional.testApp.model.Exprs.*;
import static org.junit.Assert.assertEquals;

public class BinOpTransformerTest {
  @Test
  public void balanceUpAfterRtLeft() {
    PlusExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(mul(target = plus(id(), id()), id()));
    new ExprBinOpTransformer().balanceUp(target);

    assertEquals("(id '+' (id '*' id))", container.expr.get().toString());
  }

  @Test
  public void balanceUpAfterRtRight() {
    PlusExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(mul(id(), target = plus(id(), id())));
    new ExprBinOpTransformer().balanceUp(target);

    assertEquals("((id '*' id) '+' id)", container.expr.get().toString());
  }

  @Test
  public void balanceUpRestoresAssociativityLeft() {
    PlusExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(plus(id(), target = plus(id(), id())));
    new ExprBinOpTransformer().balanceUp(target);

    assertEquals("((id '+' id) '+' id)", container.expr.get().toString());
  }

  @Test
  public void balanceUpRestoresAssociativityRight() {
    AssignExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(assign(id(), target = assign(id(), id())));
    new ExprBinOpTransformer().balanceUp(target);

    assertEquals("(id '=' (id '=' id))", container.expr.get().toString());
  }

  @Test
  public void signChangeInCaseOfEqualPriority() {
    PlusExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(plus(id(), target = plus(id(), id())));
    new ExprBinOpTransformer().balanceOnOperationChange(target);

    assertEquals("((id '+' id) '+' id)", container.expr.get().toString());
  }

  @Test
  public void simpleSignChangeOnRight() {
    BinExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(target = mul(plus(id(), id()), id()));
    new ExprBinOpTransformer().balanceOnOperationChange(target);

    assertEquals("(id '+' (id '*' id))", container.expr.get().toString());
  }

  @Test
  public void simpleSignChangeOnLeft() {
    BinExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(plus(target = mul(id(), id()), id()));
    new ExprBinOpTransformer().balanceOnOperationChange(target);

    assertEquals("((id '*' id) '+' id)", container.expr.get().toString());
  }

  @Test
  public void samePriorityChange() {
    BinExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(target = minus(id(), mul(id(), id())));
    new ExprBinOpTransformer().balanceOnOperationChange(target);

    assertEquals("(id '-' (id '*' id))", container.expr.get().toString());
  }

  @Test
  public void operationChangeRequiresAssociativityRestoration() {
    BinExpr target;
    ExprContainer container = new ExprContainer();
    container.expr.set(target = mul(id(), mul(id(), id())));

    new ExprBinOpTransformer().balanceOnOperationChange(target);

    assertEquals("((id '*' id) '*' id)", container.expr.get().toString());

  }
}