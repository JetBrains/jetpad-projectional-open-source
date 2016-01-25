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
package jetbrains.jetpad.projectional.binOp;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.testApp.model.*;

public class ExprBinOpTransformer extends BinOpTransformer<Expr, BinExpr> {
  @Override
  protected Property<Expr> left(BinExpr binExpr) {
    return binExpr.left;
  }

  @Override
  protected Property<Expr> right(BinExpr binExpr) {
    return binExpr.right;
  }

  @Override
  protected Expr parent(Expr expr) {
    if (expr == null) return null;
    ExprNode parent = expr.parent().get();
    if (parent instanceof Expr) {
      return (Expr) parent;
    }
    return null;
  }

  @Override
  protected BinExpr asBinOp(Expr expr) {
    if (expr instanceof BinExpr) {
      return (BinExpr) expr;
    }
    return null;
  }

  @Override
  protected Associativity getAssociativity(BinExpr binExpr) {
    if (binExpr instanceof AssignExpr) {
      return Associativity.RIGHT;
    } else {
      return Associativity.LEFT;
    }
  }

  @Override
  protected int getPriority(BinExpr binExpr) {
    if (binExpr instanceof AssignExpr) {
      return 0;
    }

    if (binExpr instanceof PlusExpr || binExpr instanceof MinusExpr) {
      return 1;
    }
    if (binExpr instanceof MulExpr || binExpr instanceof DivExpr) {
      return 2;
    }

    throw new IllegalStateException();
  }

  @Override
  protected void replace(Expr expr, Expr replacement) {
    if (replacement.parent().get() != null) {
      replacement.removeFromParent();
    }
    expr.replaceWith(replacement);
  }
}