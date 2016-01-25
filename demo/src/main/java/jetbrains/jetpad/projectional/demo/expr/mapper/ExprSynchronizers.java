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
package jetbrains.jetpad.projectional.demo.expr.mapper;

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Validators;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.completion.BaseCompletionItem;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.expr.model.*;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

public class ExprSynchronizers {
  private static final MapperFactory<Expression, Cell> MAPPER_FACTORY = new MapperFactory<Expression, Cell>() {
    @Override
    public Mapper<? extends Expression, ? extends Cell> createMapper(Expression source) {
      if (source instanceof PlusExpression) {
        return new BinaryExpressionMapper("+", (BinaryExpression) source);
      }

      if (source instanceof MinusExpression) {
        return new BinaryExpressionMapper("-", (BinaryExpression) source);
      }

      if (source instanceof DivExpression) {
        return new BinaryExpressionMapper("/", (BinaryExpression) source);
      }

      if (source instanceof MulExpression) {
        return new BinaryExpressionMapper("*", (BinaryExpression) source);
      }

      if (source instanceof NumberExpression) {
        return new NumberExpressionMapper((NumberExpression) source);
      }

      if (source instanceof ParensExpression) {
        return new ParensExpressionMapper((ParensExpression) source);
      }

      return new ExpressionMapper(source);
    }
  };

  public static ProjectionalRoleSynchronizer<ExpressionAstNode, Expression> expressionSynchronizer(
      Mapper<? extends ExpressionAstNode, ? extends Cell> mapper,
      Property<Expression> expr,
      Cell target) {

    ProjectionalRoleSynchronizer<ExpressionAstNode, Expression> sync = ProjectionalSynchronizers.<ExpressionAstNode, Expression>forSingleRole(mapper, expr, target, MAPPER_FACTORY);
    sync.setItemFactory(new Supplier<Expression>() {
      @Override
      public Expression get() {
        return new Expression();
      }
    });
    sync.setCompletion(new RoleCompletion<ExpressionAstNode, Expression>() {
      @Override
      public CompletionSupplier createRoleCompletion(Mapper<?, ?> mapper, ExpressionAstNode contextNode, Role<Expression> target) {
        return exprCompletion(target);
      }
    });
    sync.addMapperProcessor(ExprSideTransformMapperProcessor.INSTANCE);
    return sync;
  }

  private static CompletionSupplier exprCompletion(final Role<Expression> target) {
    return CompletionSupplier.create(
      new SimpleCompletionItem("+") {
        @Override
        public Runnable complete(String text) {
          return target.set(new PlusExpression());
        }
      },
      new SimpleCompletionItem("-") {
        @Override
        public Runnable complete(String text) {
          return target.set(new MinusExpression());
        }
      },
      new SimpleCompletionItem("/") {
        @Override
        public Runnable complete(String text) {
          return target.set(new DivExpression());
        }
      },
      new SimpleCompletionItem("*") {
        @Override
        public Runnable complete(String text) {
          return target.set(new MulExpression());
        }
      },
      new SimpleCompletionItem("(expr)") {
        @Override
        public Runnable complete(String text) {
          return target.set(new ParensExpression());
        }
      },
      new BaseCompletionItem() {
        @Override
        public String visibleText(String text) {
          return "number";
        }

        @Override
        public boolean isStrictMatchPrefix(String text) {
          if ("".equals(text)) return true;
          return isMatch(text);
        }

        @Override
        public boolean isMatch(String text) {
          return Validators.unsignedInteger().apply(text);
        }

        @Override
        public Runnable complete(String text) {
          NumberExpression numberExpr = new NumberExpression();
          if (text == null || text.isEmpty()) {
            numberExpr.value.set(0);
          } else {
            numberExpr.value.set(Integer.parseInt(text));
          }
          return target.set(numberExpr);
        }
      }
    );
  }


}