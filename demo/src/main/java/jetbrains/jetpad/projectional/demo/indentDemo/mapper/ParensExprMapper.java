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

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.indentDemo.model.ParensExpr;

class ParensExprMapper extends Mapper<ParensExpr, ParensExprMapper.ParensExprCell> {
  ParensExprMapper(ParensExpr source) {
    super(source, new ParensExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().expr, getTarget().expr));
  }

  static class ParensExprCell extends IndentCell {
    final IndentCell expr = new IndentCell();

    ParensExprCell() {
      CellFactory.to(this, CellFactory.label("(", true, false), expr, CellFactory.label(")", false, true));
    }
  }
}