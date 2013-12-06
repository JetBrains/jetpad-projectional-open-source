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
package jetbrains.jetpad.projectional.demo.diagram;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.demo.diagram.mapper.RootSimpleDiagramMapper;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramConnection;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNodeConnection;
import jetbrains.jetpad.projectional.demo.diagram.model.SimpleDiagram;
import jetbrains.jetpad.projectional.view.PolyLineView;
import jetbrains.jetpad.projectional.view.PolygonView;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.values.Color;

import java.util.Arrays;

public class DiagramDemo {
  public static ViewContainer createContainer() {
    SimpleDiagram diagram = DiagramDemo.createModel();
    RootSimpleDiagramMapper rootMapper = new RootSimpleDiagramMapper(diagram);
    rootMapper.attachRoot();
    return rootMapper.getTarget();
  }

  public static SimpleDiagram createModel() {
    SimpleDiagram result = new SimpleDiagram();

    DiagramNode n1 = new DiagramNode();
    n1.location.set(new Vector(10, 10));

    DiagramNode n2 = new DiagramNode();
    n2.location.set(new Vector(200, 10));

    DiagramNode n3 = new DiagramNode();
    n3.location.set(new Vector(10, 200));

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
}