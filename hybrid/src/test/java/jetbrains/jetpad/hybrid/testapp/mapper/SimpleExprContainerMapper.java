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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.hybrid.SimpleHybridSynchronizer;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import jetbrains.jetpad.mapper.Mapper;

public class SimpleExprContainerMapper extends Mapper<SimpleExprContainer, HorizontalCell> {
  public final SimpleHybridSynchronizer<Expr> hybridSync;

  public SimpleExprContainerMapper(SimpleExprContainer source) {
    super(source, new HorizontalCell());
    hybridSync = new SimpleHybridSynchronizer<>(this, getSource().expr, getTarget(), new ExprHybridEditorSpec());
    hybridSync.setMapperFactory(new ExprMapperFactory());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(hybridSync);
  }
}
