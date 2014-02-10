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
package jetbrains.jetpad.projectional.demo.indentDemo.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.demo.indentDemo.hybrid.LambdaHybridPositionSpec;
import jetbrains.jetpad.projectional.demo.indentDemo.model.Expr;
import jetbrains.jetpad.projectional.demo.indentDemo.model.LambdaExpr;
import jetbrains.jetpad.projectional.demo.indentDemo.model.LambdaNode;

class LambdaSynchronizers {
  static Synchronizer exprSynchronizer(
      Mapper<? extends LambdaNode, ? extends Cell> mapper,
      Property<Expr> exprProp,
      Cell targetCell) {
    HybridSynchronizer<Expr> result = new HybridSynchronizer<>(mapper, exprProp, targetCell, new LambdaHybridPositionSpec());

    result.setMapperFactory(new MapperFactory<Object, Cell>() {
      @Override
      public Mapper<?, ? extends Cell> createMapper(Object source) {
        if (source instanceof LambdaExpr) {
          return new LambdaExprMapper((LambdaExpr) source);
        }

        return null;
      }
    });

    return result;
  }
}