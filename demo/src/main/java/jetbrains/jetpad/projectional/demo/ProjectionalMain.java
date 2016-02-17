/*
 * Copyright 2012-2016 JetBrains s.r.o
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
import com.google.gwt.dom.client.Element;
import elemental.client.Browser;
import elemental.dom.Document;
import jetbrains.jetpad.cell.toDom.CellContainerToDomMapper;
import jetbrains.jetpad.projectional.demo.concept.ConceptDemo;
import jetbrains.jetpad.projectional.demo.diagram.DiagramDemo;
import jetbrains.jetpad.projectional.demo.diagramExpr.DiagramExprDemo;
import jetbrains.jetpad.projectional.demo.expr.ExprDemo;
import jetbrains.jetpad.projectional.demo.hybridExpr.HybridExprDemo;
import jetbrains.jetpad.projectional.demo.indentDemo.IndentDemo;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;

public class ProjectionalMain implements EntryPoint {
  @Override
  public void onModuleLoad() {
    Document doc = Browser.getDocument();

    new CellContainerToDomMapper(ConceptDemo.create(), (Element) doc.getElementById("conceptDemo")).attachRoot();
    new CellContainerToDomMapper(ExprDemo.createDemo(), (Element) doc.getElementById("#exprDemo")).attachRoot();
    new CellContainerToDomMapper(IndentDemo.create(), (Element) doc.getElementById("#indentDemo")).attachRoot();
    new CellContainerToDomMapper(HybridExprDemo.createDemo(), (Element) doc.getElementById("#hybridExprDemo")).attachRoot();
    ViewToDom.map(DiagramDemo.createContainer(), (Element) doc.getElementById("diagramDemo"));
    ViewToDom.map(DiagramExprDemo.createContainer(), (Element) doc.getElementById("diagramExprDemo"));
  }
}