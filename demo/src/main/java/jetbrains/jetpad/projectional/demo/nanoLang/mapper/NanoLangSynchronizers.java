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
package jetbrains.jetpad.projectional.demo.nanoLang.mapper;

import com.google.common.base.Supplier;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.action.CellAction;
import jetbrains.jetpad.projectional.cell.support.*;
import jetbrains.jetpad.projectional.demo.nanoLang.model.*;

import java.util.ArrayList;
import java.util.List;

class NanoLangSynchronizers {
  static Synchronizer expressionSynchronizer(Mapper<? extends NanoLangNode, ? extends Cell> mapper, Property<Expression> source, Cell target) {
    ProjectionalRoleSynchronizer<NanoLangNode, Expression> result = ProjectionalSynchronizers.forSingleRole(mapper, source, target, expressionMapperFactory());
    result.setCompletion(expressionCompletion());
    result.setItemFactory(createDefaultItemFactory());
    return result;
  }

  static Synchronizer expressionListSynchronizer(Mapper<? extends NanoLangNode, ? extends Cell> mapper, ObservableList<Expression> source, Cell target, List<Cell> targetList) {
    ProjectionalRoleSynchronizer<NanoLangNode, Expression> result = ProjectionalSynchronizers.forRole(mapper, source, target, targetList, expressionMapperFactory());
    result.setCompletion(expressionCompletion());
    result.setItemFactory(createDefaultItemFactory());
    return result;
  }

  private static Supplier<Expression> createDefaultItemFactory() {
    return new Supplier<Expression>() {
      @Override
      public Expression get() {
        return new DummyExpression();
      }
    };
  }

  private static RoleCompletion<NanoLangNode, Expression> expressionCompletion() {
    return new RoleCompletion<NanoLangNode, Expression>() {
      @Override
      public List<CompletionItem> createRoleCompletion(CompletionParameters ctx, Mapper<?, ? extends Cell> mapper, NanoLangNode contextNode, final Role<Expression> target) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        result.add(new SimpleCompletionItem("seq") {
          @Override
          public CellAction complete(String text) {
            return target.set(new SeqExpression());
          }
        });

        result.add(new SimpleCompletionItem("lambda") {
          @Override
          public CellAction complete(String text) {
            LambdaExpression result = new LambdaExpression();
            result.argumentName.set("x");
            return target.set(result);
          }
        });

        result.add(new SimpleCompletionItem("x") {
          @Override
          public CellAction complete(String text) {
            VarExpression var = new VarExpression();
            var.name.set("x");
            return target.set(var);
          }
        });

        return result;
      }
    };
  }

  private static MapperFactory<Expression, Cell> expressionMapperFactory() {
    return new MapperFactory<Expression, Cell>() {
      @Override
      public Mapper<? extends Expression, ? extends Cell> createMapper(Expression source) {
        if (source instanceof LambdaExpression) {
          return new LambdaExpressionMapper((LambdaExpression) source);
        }

        if (source instanceof SeqExpression) {
          return new SeqExpressionMapper((SeqExpression) source);
        }

        if (source instanceof VarExpression) {
          return new VarExpressionMapper((VarExpression) source);
        }

        if (source instanceof DummyExpression) {
          return new DummyExpressionMapper((DummyExpression) source);
        }

        return null;
      }
    };
  }
}