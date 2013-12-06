/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.demo.diagramExpr;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.demo.diagramExpr.mapper.RootSimpleDiagramMapper;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramConnection;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNodeConnection;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.SimpleDiagram;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.Expression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.MulExpression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.NumberExpression;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.PlusExpression;
import jetbrains.jetpad.projectional.view.ViewContainer;

import java.util.Arrays;

public class DiagramExprDemo {
  public static ViewContainer createContainer() {
    SimpleDiagram model = DiagramExprDemo.createModel();
    RootSimpleDiagramMapper rootMapper = new RootSimpleDiagramMapper(model);
    rootMapper.attachRoot();
    return rootMapper.getTarget();
  }

  private static SimpleDiagram createModel() {
    SimpleDiagram result = new SimpleDiagram();

    DiagramNode n1 = new DiagramNode();
    n1.location.set(new Vector(10, 10));
    n1.expression.set(createExpression());

    DiagramNode n2 = new DiagramNode();
    n2.location.set(new Vector(200, 10));
    n2.expression.set(createExpression());

    DiagramNode n3 = new DiagramNode();
    n3.location.set(new Vector(10, 200));
    n3.expression.set(createExpression());

    DiagramNodeConnection n3c = new DiagramNodeConnection();
    n3c.target.set(n1);
    n3.connections.add(n3c);

    result.nodes.addAll(Arrays.asList(n1, n2, n3));

    DiagramConnection c = new DiagramConnection();
    c.from.set(n1);
    c.to.set(n2);

    result.connections.add(c);

    return result;
  }

  private static Expression createExpression() {
    PlusExpression plus = new PlusExpression();
    plus.left.set(numberExpr(1));
    MulExpression right = new MulExpression();
    plus.right.set(right);
    right.left.set(numberExpr(2));
    right.right.set(numberExpr(3));
    return plus;
  }

  private static NumberExpression numberExpr(int value) {
    NumberExpression result = new NumberExpression();
    result.value.set(value);
    return result;
  }
}