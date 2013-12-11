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
package jetbrains.jetpad.projectional.demo.expr.mapper;

import com.google.common.base.Supplier;
import jetbrains.jetpad.mapper.*;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.action.CellAction;
import jetbrains.jetpad.projectional.cell.completion.BaseCompletionItem;
import jetbrains.jetpad.projectional.cell.completion.CompletionItem;
import jetbrains.jetpad.projectional.cell.completion.CompletionParameters;
import jetbrains.jetpad.projectional.cell.completion.SimpleCompletionItem;
import jetbrains.jetpad.projectional.demo.expr.model.*;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.support.*;
import jetbrains.jetpad.projectional.util.Validators;

import java.util.ArrayList;
import java.util.List;

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
      public List<CompletionItem> createRoleCompletion(CompletionParameters cp, Mapper<?, ? extends Cell> mapper, ExpressionAstNode contextNode, Role<Expression> target) {
        return exprCompletion(target);
      }
    });
    sync.addMapperProcessor(ExprSideTransformMapperProcessor.INSTANCE);
    return sync;
  }

  private static List<CompletionItem> exprCompletion(final Role<Expression> target) {
    List<CompletionItem> result = new ArrayList<CompletionItem>();
    result.add(new SimpleCompletionItem("+") {
      @Override
      public CellAction complete(String text) {
        return target.set(new PlusExpression());
      }
    });
    result.add(new SimpleCompletionItem("-") {
      @Override
      public CellAction complete(String text) {
        return target.set(new MinusExpression());
      }
    });
    result.add(new SimpleCompletionItem("/") {
      @Override
      public CellAction complete(String text) {
        return target.set(new DivExpression());
      }
    });
    result.add(new SimpleCompletionItem("*") {
      @Override
      public CellAction complete(String text) {
        return target.set(new MulExpression());
      }
    });
    result.add(new SimpleCompletionItem("(expr)") {
      @Override
      public CellAction complete(String text) {
        return target.set(new ParensExpression());
      }
    });

    result.add(new BaseCompletionItem() {
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
        return Validators.integer().apply(text);
      }

      @Override
      public CellAction complete(String text) {
        NumberExpression numberExpr = new NumberExpression();
        if (text == null || text.isEmpty()) {
          numberExpr.value.set(0);
        } else {
          numberExpr.value.set(Integer.parseInt(text));
        }
        return target.set(numberExpr);
      }
    });
    return result;
  }


}