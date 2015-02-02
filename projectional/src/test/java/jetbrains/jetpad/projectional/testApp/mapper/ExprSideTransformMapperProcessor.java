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
package jetbrains.jetpad.projectional.testApp.mapper;

import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperProcessor;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.testApp.model.*;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.model.composite.Composites.*;

class ExprSideTransformMapperProcessor implements MapperProcessor<Expr, Cell> {
  static final ExprSideTransformMapperProcessor INSTANCE = new ExprSideTransformMapperProcessor();

  @Override
  public void process(final Mapper<? extends Expr, ? extends Cell> mapper) {
    final Cell cell = mapper.getTarget();
    final Expr expr = mapper.getSource();

    Cell firstLeaf = firstFocusable(cell);
    Cell lastLeaf = lastFocusable(cell);

    if (firstLeaf != null) {
      firstLeaf.addTrait(new CellTrait() {

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
                    return rightTransform(mapper, new PlusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new MinusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new MulExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new DivExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("=") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new AssignExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("==") {
                  @Override
                  public Runnable complete(String text) {
                    return rightTransform(mapper, new EqExpr(), expr);
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

    if (lastLeaf != null) {
      lastLeaf.addTrait(new CellTrait() {
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
                    return leftTransform(mapper, new PlusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new MinusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new MulExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new DivExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("=") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new AssignExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("==") {
                  @Override
                  public Runnable complete(String text) {
                    return leftTransform(mapper, new EqExpr(), expr);
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

  private Runnable leftTransform(Mapper<?, ?> sourceMapper, BinExpr newExpr, Expr expr) {
    Mapper<?, ?> parentMapper = sourceMapper.getParent();
    expr.replaceWith(newExpr);
    newExpr.right.set(expr);

    Cell target = (Cell) parentMapper.getDescendantMapper(newExpr).getTarget();
    return selectExpression(target);
  }

  private Runnable rightTransform(Mapper<?, ?> sourceMapper, BinExpr newExpr, Expr expr) {
    Mapper<?, ?> parentMapper = sourceMapper.getParent();
    expr.replaceWith(newExpr);
    newExpr.left.set(expr);

    Cell target = (Cell) parentMapper.getDescendantMapper(newExpr).getTarget();
    return selectExpression(target);
  }

  private static Runnable selectExpression(Cell pos) {
    return pos.get(ProjectionalSynchronizers.ON_CREATE);
  }
}