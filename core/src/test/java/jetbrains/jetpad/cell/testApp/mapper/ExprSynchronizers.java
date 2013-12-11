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
package jetbrains.jetpad.cell.testApp.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.cell.testApp.model.Expr;
import jetbrains.jetpad.cell.testApp.model.ExprNode;

class ExprSynchronizers {
  static ProjectionalRoleSynchronizer<ExprNode, Expr> forSingleRole(final Mapper<? extends ExprNode, ? extends Cell> mapper, Property<Expr> prop, Cell target) {
    ProjectionalRoleSynchronizer<ExprNode, Expr> result = ProjectionalSynchronizers.<ExprNode, Expr>forSingleRole(mapper, prop, target, ExprMapperFactory.INSTANCE);
    result.setItemFactory(new DefaultExprSupplier());
    result.setCompletion(new ExprCompletion());
    result.addMapperProcessor(ExprSideTransformMapperProcessor.INSTANCE);
    return result;
  }
}