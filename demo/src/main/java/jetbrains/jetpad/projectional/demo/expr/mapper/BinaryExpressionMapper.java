/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.demo.expr.mapper;

import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Mappers;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.demo.expr.ExprBinOpTransformer;
import jetbrains.jetpad.projectional.demo.expr.model.*;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.projectional.cell.*;

import java.util.ArrayList;
import java.util.List;

class BinaryExpressionMapper extends Mapper<BinaryExpression, BinaryExpressionCell> {
  BinaryExpressionMapper(String sign, BinaryExpression source) {
    super(source, new BinaryExpressionCell(sign));
    getTarget().sign.addTrait(new BaseCellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new SignCompletion(BinaryExpressionMapper.this);
        }

        return super.get(cell, spec);
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    ProjectionalRoleSynchronizer<ExpressionAstNode, Expression> leftSync = ExprSynchronizers.expressionSynchronizer(this, getSource().left, getTarget().left);
    leftSync.setDeleteHandler(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public Runnable delete() {
        Mapper<?, ?> parentMapper = getParent();
        Expression right = getSource().right.get();
        right.removeFromParent();
        getSource().replaceWith(right);
        return CellActions.toFirstFocusable((Cell) parentMapper.getDescendantMapper(right).getTarget());
      }
    });
    conf.add(leftSync);

    ProjectionalRoleSynchronizer<ExpressionAstNode, Expression> rightSync = ExprSynchronizers.expressionSynchronizer(this, getSource().right, getTarget().right);
    rightSync.setDeleteHandler(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public Runnable delete() {
        Mapper<?, ?> parentMapper = getParent();
        Expression left = getSource().left.get();
        left.removeFromParent();
        getSource().replaceWith(left);
        return CellActions.toFirstFocusable((Cell) parentMapper.getDescendantMapper(left).getTarget());
      }
    });

    conf.add(rightSync);
  }

  static class SignCompletion implements CompletionSupplier {
    private BinaryExpressionMapper myMapper;

    SignCompletion(BinaryExpressionMapper mapper) {
      myMapper = mapper;
    }

    @Override
    public List<CompletionItem> get(CompletionParameters cp) {
      List<CompletionItem> result = new ArrayList<>();
      result.add(new SimpleCompletionItem("+") {
        @Override
        public Runnable complete(String text) {
          return replaceWith(new PlusExpression());
        }
      });
      result.add(new SimpleCompletionItem("-") {
        @Override
        public Runnable complete(String text) {
          return replaceWith(new MinusExpression());
        }
      });
      result.add(new SimpleCompletionItem("*") {
        @Override
        public Runnable complete(String text) {
          return replaceWith(new MulExpression());
        }
      });
      result.add(new SimpleCompletionItem("/") {
        @Override
        public Runnable complete(String text) {
          return replaceWith(new DivExpression());
        }
      });
      return result;
    }

    private Runnable replaceWith(final BinaryExpression newExpr) {
      final Mapper<?, ?> root = Mappers.getRoot(myMapper);

      BinaryExpression oldExpr = myMapper.getSource();
      oldExpr.replaceWith(newExpr);

      Expression left = oldExpr.left.get();
      if (left != null) {
        left.removeFromParent();
      }
      Expression right = oldExpr.right.get();
      if (right != null) {
        right.removeFromParent();
      }

      newExpr.left.set(left);
      newExpr.right.set(right);

      new ExprBinOpTransformer().balanceOnOperationChange(newExpr).parent().get();

      return CellActions.toEnd(((BinaryExpressionMapper) root.getDescendantMapper(newExpr)).getTarget().sign);
    }
  }
}