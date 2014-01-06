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
package jetbrains.jetpad.projectional.demo.diagram;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.demo.diagram.mapper.RootSimpleDiagramMapper;
import jetbrains.jetpad.projectional.demo.diagram.model.SimpleDiagram;
import jetbrains.jetpad.projectional.view.PolyLineView;
import jetbrains.jetpad.projectional.view.PolygonView;
import jetbrains.jetpad.projectional.view.awt.AwtDemo;
import jetbrains.jetpad.values.Color;

public class DiagramDemoMain {
  public static void main(String[] args) {
    SimpleDiagram model = DiagramDemo.createModel();

    RootSimpleDiagramMapper rootMapper = new RootSimpleDiagramMapper(model);
    rootMapper.attachRoot();

    AwtDemo.show(rootMapper.getTarget());
  }
}