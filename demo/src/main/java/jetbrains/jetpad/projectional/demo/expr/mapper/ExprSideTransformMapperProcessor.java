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

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperProcessor;
import jetbrains.jetpad.mapper.Mappers;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.projectional.demo.expr.ExprBinOpTransformer;
import jetbrains.jetpad.projectional.demo.expr.model.*;

import java.util.ArrayList;
import java.util.List;

class ExprSideTransformMapperProcessor implements MapperProcessor<Expression, Cell> {
  static final ExprSideTransformMapperProcessor INSTANCE = new ExprSideTransformMapperProcessor();

  @Override
  public void process(final Mapper<? extends Expression, ? extends Cell> mapper) {
    final Cell cell = mapper.getTarget();
    final Expression expr = mapper.getSource();

    Cell firstFocusable = Composites.firstFocusable(cell);
    Cell lastFocusable = Composites.lastFocusable(cell);

    if (lastFocusable != null) {
      lastFocusable.addTrait(new BaseCellTrait() {
        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.RIGHT_TRANSFORM) {
            return new CompletionSupplier() {
              @Override
              public List<CompletionItem> get(CompletionParameters cp) {
                List<CompletionItem> result = new ArrayList<>();
                result.add(new SimpleCompletionItem("+") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new PlusExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new MinusExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new MulExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new DivExpression(), expr);
                  }
                });
                return result;
              }
            };
          }

          return super.get(cell, spec);
        }
      });
    }

    if (firstFocusable != null) {

      firstFocusable.addTrait(new BaseCellTrait() {
        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.LEFT_TRANSFORM) {
            return new CompletionSupplier() {
              @Override
              public List<CompletionItem> get(CompletionParameters cp) {
                List<CompletionItem> result = new ArrayList<>();
                result.add(new SimpleCompletionItem("+") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new PlusExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new MinusExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new MulExpression(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new DivExpression(), expr);
                  }
                });
                return result;
              }
            };
          }

          return super.get(cell, spec);
        }
      });
    }
  }


  private static Runnable leftTransform(Mapper<? extends Expression, ?> mapper, final BinaryExpression newExpr, final Expression expr) {
    return doTransform(mapper, newExpr, new Handler<Expression>() {
      @Override
      public void handle(Expression placeholder) {
        newExpr.left.set(placeholder);
        newExpr.right.set(expr);
      }
    });
  }

  private static Runnable rightTransform(Mapper<? extends Expression, ?> mapper, final BinaryExpression newExpr, final Expression expr) {
    return doTransform(mapper, newExpr, new Handler<Expression>() {
      @Override
      public void handle(Expression placeholder) {
        newExpr.left.set(expr);
        newExpr.right.set(placeholder);
      }
    });
  }

  private static Runnable doTransform(final Mapper<? extends Expression, ?> mapper, final BinaryExpression newExpr, final Handler<Expression> handler) {
    Mapper<?, ?> root = Mappers.getRoot(mapper);

    final Expression placeholder = new Expression();
    Expression expr = mapper.getSource();
    expr.replaceWith(newExpr);
    handler.handle(placeholder);

    new ExprBinOpTransformer().balanceUp(newExpr);

    Mapper<?, ? extends Cell> placeholderMapper = (Mapper<?, ? extends Cell>) root.getDescendantMapper(placeholder);
    final Cell targetCell = placeholderMapper.getTarget().parent().get();

    placeholder.removeFromParent();

    return CellActions.toFirstFocusable(targetCell);
  }
}