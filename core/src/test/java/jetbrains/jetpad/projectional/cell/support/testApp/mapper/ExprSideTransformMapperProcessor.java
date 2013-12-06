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
package jetbrains.jetpad.projectional.cell.support.testApp.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperProcessor;
import jetbrains.jetpad.projectional.cell.BaseCellTrait;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.Cells;
import jetbrains.jetpad.projectional.cell.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.cell.support.*;
import jetbrains.jetpad.projectional.cell.support.testApp.model.*;

import java.util.ArrayList;
import java.util.List;

class ExprSideTransformMapperProcessor implements MapperProcessor<Expr, Cell> {
  static final ExprSideTransformMapperProcessor INSTANCE = new ExprSideTransformMapperProcessor();

  @Override
  public void process(final Mapper<? extends Expr, ? extends Cell> mapper) {
    final Cell cell = mapper.getTarget();
    final Expr expr = mapper.getSource();

    Cell firstLeaf = Cells.firstFocusableLeaf(cell);
    Cell lastLeaf = Cells.lastFocusableLeaf(cell);

    if (firstLeaf != null) {
      firstLeaf.addTrait(new BaseCellTrait() {

        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.RIGHT_TRANSFORM) {
            return new CompletionSupplier() {
              @Override
              public List<CompletionItem> get(CompletionParameters cp) {
                List<CompletionItem> result = new ArrayList<CompletionItem>();
                result.add(new SimpleCompletionItem("+") {
                  @Override
                  public CellAction complete(String text) {
                    return rightTransform(mapper, new PlusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public CellAction complete(String text) {
                    return rightTransform(mapper, new MinusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public CellAction complete(String text) {
                    return rightTransform(mapper, new MulExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public CellAction complete(String text) {
                    return rightTransform(mapper, new DivExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("=") {
                  @Override
                  public CellAction complete(String text) {
                    return rightTransform(mapper, new AssignExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("==") {
                  @Override
                  public CellAction complete(String text) {
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
      lastLeaf.addTrait(new BaseCellTrait() {
        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.LEFT_TRANSFORM) {
            return new CompletionSupplier() {
              @Override
              public List<CompletionItem> get(CompletionParameters cp) {
                List<CompletionItem> result = new ArrayList<CompletionItem>();
                result.add(new SimpleCompletionItem("+") {
                  @Override
                  public CellAction complete(String text) {
                    return leftTransform(mapper, new PlusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("-") {
                  @Override
                  public CellAction complete(String text) {
                    return leftTransform(mapper, new MinusExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("*") {
                  @Override
                  public CellAction complete(String text) {
                    return leftTransform(mapper, new MulExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("/") {
                  @Override
                  public CellAction complete(String text) {
                    return leftTransform(mapper, new DivExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("=") {
                  @Override
                  public CellAction complete(String text) {
                    return leftTransform(mapper, new AssignExpr(), expr);
                  }
                });
                result.add(new SimpleCompletionItem("==") {
                  @Override
                  public CellAction complete(String text) {
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

  private CellAction leftTransform(Mapper<?, ?> sourceMapper, BinExpr newExpr, Expr expr) {
    Mapper<?, ?> parentMapper = sourceMapper.getParent();
    expr.replaceWith(newExpr);
    newExpr.right.set(expr);

    Cell target = (Cell) parentMapper.getDescendantMapper(newExpr).getTarget();
    return selectExpression(target);
  }

  private CellAction rightTransform(Mapper<?, ?> sourceMapper, BinExpr newExpr, Expr expr) {
    Mapper<?, ?> parentMapper = sourceMapper.getParent();
    expr.replaceWith(newExpr);
    newExpr.left.set(expr);

    Cell target = (Cell) parentMapper.getDescendantMapper(newExpr).getTarget();
    return selectExpression(target);
  }

  private static CellAction selectExpression(Cell pos) {
    return pos.get(ProjectionalSynchronizers.ON_CREATE);
  }
}