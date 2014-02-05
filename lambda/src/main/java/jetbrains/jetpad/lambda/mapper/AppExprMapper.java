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

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.lambda.model.AppExpr;

import static jetbrains.jetpad.cell.util.CellFactory.space;
import static jetbrains.jetpad.cell.util.CellFactory.to;

class AppExprMapper extends Mapper<AppExpr, AppExprMapper.AppExprCell> {
  AppExprMapper(AppExpr source) {
    super(source, new AppExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().fun, getTarget().fun));
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().arg, getTarget().arg));
  }

  static final class AppExprCell extends IndentCell {
    final IndentCell fun = new IndentCell();
    final IndentCell arg = new IndentCell();

    AppExprCell() {
      focusable().set(true);
      to(this, CellFactory.label("(", true, false), fun, space(), arg, CellFactory.label(")", false, true));
    }
  }
}