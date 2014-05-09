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
package jetbrains.jetpad.projectional.demo;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.cell.toDom.CellContainerToDomMapper;
import jetbrains.jetpad.projectional.demo.concept.ConceptDemo;
import jetbrains.jetpad.projectional.demo.diagram.DiagramDemo;
import jetbrains.jetpad.projectional.demo.diagramExpr.DiagramExprDemo;
import jetbrains.jetpad.projectional.demo.expr.ExprDemo;
import jetbrains.jetpad.projectional.demo.hybridExpr.HybridExprDemo;
import jetbrains.jetpad.projectional.demo.indentDemo.IndentDemo;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;

import static com.google.gwt.query.client.GQuery.$;

public class ProjectionalMain implements EntryPoint {
  @Override
  public void onModuleLoad() {
    new CellContainerToDomMapper(ConceptDemo.create(), $("#conceptDemo").get(0)).attachRoot();
    new CellContainerToDomMapper(ExprDemo.createDemo(), $("#exprDemo").get(0)).attachRoot();
    new CellContainerToDomMapper(IndentDemo.create(), $("#indentDemo").get(0)).attachRoot();
    new CellContainerToDomMapper(HybridExprDemo.createDemo(), $("#hybridExprDemo").get(0)).attachRoot();
    ViewToDom.map(DiagramDemo.createContainer(), $("#diagramDemo").get(0));
    ViewToDom.map(DiagramExprDemo.createContainer(), $("#diagramExprDemo").get(0));
  }
}