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

public abstract class BinOpTransformer<ExprT, BinExprT extends ExprT> {
  protected abstract Property<ExprT> left(BinExprT binExpr);
  protected abstract Property<ExprT> right(BinExprT binExpr);
  protected abstract ExprT parent(ExprT expr);
  protected abstract BinExprT asBinOp(ExprT expr);
  protected abstract int getPriority(BinExprT binExpr);
  protected abstract Associativity getAssociativity(BinExprT binExpr);
  protected abstract void replace(ExprT expr, ExprT replacement);

  private boolean isBinOp(ExprT expr) {
    if (expr == null) return false;
    return asBinOp(expr) != null;
  }

  private boolean canRotateRight(BinExprT binExpr) {
    return isBinOp(left(binExpr).get());
  }

  private boolean canRotateLeft(BinExprT binExpr) {
    return isBinOp(right(binExpr).get());
  }

  private boolean isLeftChild(ExprT expr) {
    ExprT parent = parent(expr);
    if (!isBinOp(parent)) return false;
    BinExprT parentExpr = asBinOp(parent);
    return left(parentExpr).get() == expr;
  }

  private boolean isRightChild(ExprT expr) {
    ExprT parent = parent(expr);
    if (!isBinOp(parent)) return false;
    BinExprT parentExpr = asBinOp(parent);
    return right(parentExpr).get() == expr;
  }

  private BinExprT rotateLeft(BinExprT binExpr) {
    if (!canRotateLeft(binExpr)) {
      throw new IllegalStateException();
    }
    BinExprT right = asBinOp(right(binExpr).get());
    ExprT rightLeft = left(right).get();
    left(right).set(null);
    replace(binExpr, right);
    left(right).set(binExpr);
    right(binExpr).set(rightLeft);
    return right;
  }

  private BinExprT rotateRight(BinExprT binExpr) {
    if (!canRotateRight(binExpr)) {
      throw new IllegalStateException();
    }
    BinExprT left = asBinOp(left(binExpr).get());
    ExprT leftRight = right(left).get();
    right(left).set(null);
    replace(binExpr, left);
    right(left).set(binExpr);
    left(binExpr).set(leftRight);
    return left;
  }

  public BinExprT balanceUp(BinExprT expr) {
    ExprT parent = parent(expr);

    if (!isBinOp(parent)) return expr;

    BinExprT parentNode = asBinOp(parent);
    int parentPriority = getPriority(parentNode);
    int exprPriority = getPriority(expr);
    Associativity assoc = getAssociativity(parentNode);

    if (isLeftChild(expr) && (
        (assoc == Associativity.LEFT && parentPriority > exprPriority) ||
        (assoc == Associativity.RIGHT && parentPriority >= exprPriority))) {
      return balanceUp(rotateRight(parentNode));
    }

    if (isRightChild(expr) && (
        (assoc == Associativity.LEFT && parentPriority >= exprPriority) ||
        (assoc == Associativity.RIGHT && parentPriority > exprPriority))) {
      return balanceUp(rotateLeft(parentNode));
    }

    return expr;
  }

  public ExprT balanceOnOperationChange(BinExprT expr) {
    int exprPriority = getPriority(expr);
    ExprT left = left(expr).get();
    ExprT right = right(expr).get();
    Associativity assoc = getAssociativity(expr);

    if (isBinOp(left)) {
      BinExprT leftExpr = asBinOp(left);
      int leftPriority = getPriority(leftExpr);
      if ((assoc == Associativity.LEFT && leftPriority < exprPriority) ||
          (assoc == Associativity.RIGHT && leftPriority <= exprPriority)) {
        expr = balanceDown(expr);
      }
    } else if (isBinOp(right)) {
      BinExprT rightExpr = asBinOp(right);
      int rightPriority = getPriority(rightExpr);
      if ((assoc == Associativity.LEFT && rightPriority >= exprPriority) ||
          (assoc == Associativity.RIGHT && rightPriority > exprPriority)) {
        expr = balanceDown(expr);
      }
    }

    ExprT parent = parent(expr);
    assoc = getAssociativity(expr);
    if (isBinOp(parent)) {
      BinExprT parentExpr = asBinOp(parent);
      int parentPriority = getPriority(parentExpr);
      boolean isLeft = left == left(parentExpr).get();

      if (isLeft &&
        (assoc == Associativity.LEFT && exprPriority < parentPriority) ||
        (assoc == Associativity.RIGHT && exprPriority <= parentPriority)) {
        return balanceUp(expr);
      }

      if (!isLeft &&
        (assoc == Associativity.LEFT && exprPriority <= parentPriority) ||
        (assoc == Associativity.RIGHT && exprPriority < parentPriority)) {
        return balanceUp(expr);
      }
    }

    return expr;
  }

  public BinExprT balanceDown(BinExprT expr) {
    int exprPriority = getPriority(expr);
    Associativity assoc = getAssociativity(expr);
    ExprT left = left(expr).get();
    ExprT right = right(expr).get();

    if (isBinOp(left)) {
      BinExprT leftExpr = asBinOp(left);
      int leftPriority = getPriority(leftExpr);

      if ((assoc == Associativity.LEFT && leftPriority < exprPriority) ||
          (assoc == Associativity.RIGHT && leftPriority <= exprPriority)) {
        BinExprT result = rotateRight(expr);
        balanceDown(asBinOp(right(result).get()));
        return result;
      }
    }

    if (isBinOp(right)) {
      BinExprT rightExpr = asBinOp(right);
      int rightPriority = getPriority(rightExpr);

      if ((assoc == Associativity.LEFT && rightPriority <= exprPriority) ||
          (assoc == Associativity.RIGHT && rightPriority < exprPriority)) {
        BinExprT result = rotateLeft(expr);
        balanceDown(asBinOp(left(result).get()));
        return result;
      }
    }

    return expr;
  }
}