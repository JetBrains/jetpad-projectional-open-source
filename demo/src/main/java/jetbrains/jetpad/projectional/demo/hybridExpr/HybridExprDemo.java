/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.demo.hybridExpr;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.error.ErrorStyler;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.hybridExpr.mapper.ExpressionContainerMapper;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.ExpressionContainer;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.MulExpression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.NumberExpression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.PlusExpression;
import jetbrains.jetpad.projectional.util.RootController;

public class HybridExprDemo {
  public static CellContainer createDemo(boolean isDom) {
    ExpressionContainer model = createModel();
    Mapper<?, ? extends Cell> rootMapper = new ExpressionContainerMapper(model);
    rootMapper.attachRoot();

    CellContainer cellContainer = new CellContainer();
    cellContainer.root.children().add(rootMapper.getTarget());
    RootController.install(cellContainer);

    ErrorStyler defaultStyler = null;
    if (isDom) {
      defaultStyler = new ErrorStyler() {
        @Override
        protected Registration doApplyError(Cell cell) {
          return Registration.EMPTY;
        }
      };
    }
    RootController.supportErrors(cellContainer, defaultStyler, null);

    return cellContainer;
  }

  private static ExpressionContainer createModel() {
    ExpressionContainer result = new ExpressionContainer();

    PlusExpression plus = new PlusExpression();
    plus.left.set(numberExpr(1));

    MulExpression right = new MulExpression();
    plus.right.set(right);

    right.left.set(numberExpr(2));

    right.right.set(numberExpr(3));
    result.expression.set(plus);

    return result;
  }

  private static NumberExpression numberExpr(int value) {
    NumberExpression result = new NumberExpression();
    result.value.set(value);
    return result;
  }
}