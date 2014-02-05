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
package jetbrains.jetpad.lambda.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.lambda.model.LambdaExpr;

class LambdaExprMapper extends Mapper<LambdaExpr, LambdaExprMapper.LambdaExprCell> {
  LambdaExprMapper(LambdaExpr source) {
    super(source, new LambdaExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().varName, getTarget().name.text()));
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().body, getTarget().body));
  }

  static class LambdaExprCell extends IndentCell {
    final TextCell name = new TextCell();
    final IndentCell body = new IndentCell();

    LambdaExprCell() {
      focusable().set(true);

      CellFactory.to(this, CellFactory.label("(\\", true, false), name, CellFactory.space(), CellFactory.label("->"), CellFactory.indent(true, CellFactory.newLine(), CellFactory.space(), body, CellFactory.label(")", false, true)));

      name.addTrait(TextEditing.validTextEditing(Validators.identifier()));
    }
  }
}