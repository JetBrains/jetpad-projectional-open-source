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
import jetbrains.jetpad.hybrid.HybridEditorSpec;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class ExprContainerMapper extends Mapper<ExprContainer, HorizontalCell> {
  public final HybridSynchronizer<Expr> hybridSync;
  public final Property<HybridEditorSpec<Expr>> hybridSyncSpec = new ValueProperty<HybridEditorSpec<Expr>>(new ExprHybridEditorSpec());

  public ExprContainerMapper(ExprContainer source) {
    super(source, new HorizontalCell());
    hybridSync = new HybridSynchronizer<>(this, getSource().expr, getTarget(), hybridSyncSpec);
    hybridSync.setMapperFactory(new ExprMapperFactory());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(hybridSync);
  }
}