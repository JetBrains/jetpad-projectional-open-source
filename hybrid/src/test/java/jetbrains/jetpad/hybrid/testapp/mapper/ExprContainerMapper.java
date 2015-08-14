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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.cell.Cell;

public class ExprContainerMapper extends Mapper<ExprContainer, HorizontalCell> {
  public final HybridSynchronizer<Expr> hybridSync;

  public ExprContainerMapper(ExprContainer source) {
    super(source, new HorizontalCell());
    hybridSync = new HybridSynchronizer<>(this, getSource().expr, getTarget(), new ExprHybridEditorSpec());

    hybridSync.setMapperFactory(new MapperFactory<Object, Cell>() {
      @Override
      public Mapper<?, ? extends Cell> createMapper(Object source) {
        if (source instanceof PosValueExpr) {
          return new PosValueExprMapper((PosValueExpr) source);
        }
        if (source instanceof ValueExpr) {
          return new ValueExprMapper((ValueExpr) source);
        }
        if (source instanceof AsyncValueExpr) {
          return new AsyncValueExprMapper((AsyncValueExpr) source);
        }
        if (source instanceof ComplexValueExpr) {
          return new ComplexValueExprMapper((ComplexValueExpr) source);
        }
        return null;
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(hybridSync);
  }
}