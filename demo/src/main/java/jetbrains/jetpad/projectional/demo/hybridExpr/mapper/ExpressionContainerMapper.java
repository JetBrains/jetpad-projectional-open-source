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
package jetbrains.jetpad.projectional.demo.hybridExpr.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.Expression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.ExpressionContainer;
import jetbrains.jetpad.hybrid.HybridSynchronizer;


public class ExpressionContainerMapper extends Mapper<ExpressionContainer, ExpressionContainerCell> {
  public ExpressionContainerMapper(ExpressionContainer source) {
    super(source, new ExpressionContainerCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(new HybridSynchronizer<Expression>(this, getSource().expression, getTarget().expression, new ExprHybridPositionSpec()));
  }
}