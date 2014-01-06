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
package jetbrains.jetpad.projectional.demo.nanoLang;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.projectional.demo.nanoLang.mapper.ContainerMapper;
import jetbrains.jetpad.projectional.demo.nanoLang.model.Container;
import jetbrains.jetpad.projectional.demo.nanoLang.model.LambdaExpression;
import jetbrains.jetpad.projectional.demo.nanoLang.model.SeqExpression;

public class NanoLangDemo {
  public static CellContainer create() {
    CellContainer cellContainer = new CellContainer();
    RootController.install(cellContainer);

    ContainerMapper rootMapper = new ContainerMapper(createModel());
    rootMapper.attachRoot();
    cellContainer.root.children().add(rootMapper.getTarget());

    return cellContainer;
  }

  private static Container createModel() {
    Container result = new Container();

    SeqExpression seq = new SeqExpression();

    LambdaExpression lamX = new LambdaExpression();
    lamX.argumentName.set("x");
    seq.expressions.add(lamX);

    LambdaExpression lamY = new LambdaExpression();
    lamY.argumentName.set("y");
    lamX.body.set(lamY);

    LambdaExpression lamZ = new LambdaExpression();
    lamZ.argumentName.set("z");
    lamY.body.set(lamZ);

    lamZ.body.set(new SeqExpression());


    result.expression.set(seq);

    return result;
  }
}