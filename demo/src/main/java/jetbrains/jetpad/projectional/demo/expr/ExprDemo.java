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
package jetbrains.jetpad.projectional.demo.expr;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.expr.mapper.ExpressionMappers;
import jetbrains.jetpad.projectional.demo.expr.model.ExpressionContainer;
import jetbrains.jetpad.projectional.demo.expr.model.MulExpression;
import jetbrains.jetpad.projectional.demo.expr.model.NumberExpression;
import jetbrains.jetpad.projectional.demo.expr.model.PlusExpression;
import jetbrains.jetpad.projectional.util.RootController;

public class ExprDemo {
  public static CellContainer createDemo() {
    ExpressionContainer model = createModel();
    Mapper<ExpressionContainer, ? extends Cell> rootMapper = ExpressionMappers.create(model);
    rootMapper.attachRoot();
    CellContainer cellContainer = new CellContainer();
    cellContainer.root.children().add(rootMapper.getTarget());
    RootController.install(cellContainer);
    return cellContainer;
  }

  private static ExpressionContainer createModel() {
    ExpressionContainer result = new ExpressionContainer();

    PlusExpression plus = new PlusExpression();
    NumberExpression left = new NumberExpression();
    left.value.set(1);
    plus.left.set(left);

    MulExpression right = new MulExpression();
    plus.right.set(right);

    NumberExpression right1 = new NumberExpression();
    right1.value.set(2);
    right.left.set(right1);

    NumberExpression right2 = new NumberExpression();
    right2.value.set(3);
    right.right.set(right2);

    result.expression.set(plus);

    return result;
  }
}