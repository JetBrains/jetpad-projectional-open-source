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
package jetbrains.jetpad.projectional.demo.expr;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.binOp.Associativity;
import jetbrains.jetpad.projectional.binOp.BinOpTransformer;
import jetbrains.jetpad.projectional.demo.expr.model.*;

public class ExprBinOpTransformer extends BinOpTransformer<Expression, BinaryExpression> {
  @Override
  protected Property<Expression> left(BinaryExpression binExpr) {
    return binExpr.left;
  }

  @Override
  protected Property<Expression> right(BinaryExpression binExpr) {
    return binExpr.right;
  }

  @Override
  protected Expression parent(Expression expr) {
    ExpressionAstNode node = expr.parent().get();
    if (node instanceof Expression) {
      return (Expression) node;
    } else {
      return null;
    }
  }

  @Override
  protected BinaryExpression asBinOp(Expression expr) {
    if (expr instanceof BinaryExpression) {
      return (BinaryExpression) expr;
    }
    return null;
  }

  @Override
  protected int getPriority(BinaryExpression binExpr) {
    if (binExpr instanceof PlusExpression || binExpr instanceof MinusExpression) {
      return 4;
    }

    if (binExpr instanceof MulExpression || binExpr instanceof DivExpression) {
      return 5;
    }

    throw new IllegalArgumentException();
  }

  @Override
  protected Associativity getAssociativity(BinaryExpression binExpr) {
    return Associativity.LEFT;
  }

  @Override
  protected void replace(Expression expr, Expression replacement) {
    expr.replaceWith(replacement);
  }
}