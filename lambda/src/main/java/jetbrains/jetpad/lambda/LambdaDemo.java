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
package jetbrains.jetpad.lambda;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.lambda.mapper.ContainerMapper;
import jetbrains.jetpad.lambda.model.AppExpr;
import jetbrains.jetpad.lambda.model.Container;
import jetbrains.jetpad.lambda.model.LambdaExpr;
import jetbrains.jetpad.lambda.model.VarExpr;
import jetbrains.jetpad.projectional.util.RootController;

public class LambdaDemo {
  private static Container createModel() {
    Container root = new Container();
    LambdaExpr l1 = new LambdaExpr();
    l1.varName.set("x");
    VarExpr var = new VarExpr();
    var.name.set("x");
    l1.body.set(var);

    LambdaExpr l2 = (LambdaExpr) l1.copy();

    AppExpr app = new AppExpr();
    app.fun.set(l1);
    app.arg.set(l2);

    root.expr.set(app);
    return root;
  }

  public static CellContainer create() {
    CellContainer cellContainer = new CellContainer();
    RootController.install(cellContainer);

    ContainerMapper rootMapper = new ContainerMapper(createModel());
    rootMapper.attachRoot();
    cellContainer.root.children().add(rootMapper.getTarget());
    return cellContainer;
  }
}