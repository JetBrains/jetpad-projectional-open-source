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
package jetbrains.jetpad.projectional.cell.support.testApp.model;

public class Exprs {
  public static IdExpr id() {
    return new IdExpr();
  }

  public static PlusExpr plus(Expr left, Expr right) {
    return fillBinExpr(new PlusExpr(), left, right);
  }

  public static MinusExpr minus(Expr left, Expr right) {
    return fillBinExpr(new MinusExpr(), left, right);
  }

  public static MulExpr mul(Expr left, Expr right) {
    return fillBinExpr(new MulExpr(), left, right);
  }

  public static DivExpr div(Expr left, Expr right) {
    return fillBinExpr(new DivExpr(), left, right);
  }


  public static AssignExpr assign(Expr left, Expr right) {
    return fillBinExpr(new AssignExpr(), left, right);
  }

  private static <BinExprT extends BinExpr> BinExprT fillBinExpr(BinExprT expr, Expr left, Expr right) {
    expr.left.set(left);
    expr.right.set(right);
    return expr;
  }
}