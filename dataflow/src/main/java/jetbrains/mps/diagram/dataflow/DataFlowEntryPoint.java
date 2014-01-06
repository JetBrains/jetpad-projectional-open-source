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
package jetbrains.mps.diagram.dataflow;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.projectional.view.gwt.View2Dom;
import jetbrains.mps.diagram.dataflow.mapper.RootDiagramMapper;
import jetbrains.mps.diagram.dataflow.model.Diagram;

import static com.google.gwt.query.client.GQuery.$;

public class DataFlowEntryPoint implements EntryPoint {
  @Override
  public void onModuleLoad() {
    Diagram model = Demo.createDemoModel();
    RootDiagramMapper mapper = new RootDiagramMapper(model);
    mapper.attachRoot();

    View2Dom.showDemo(mapper.getTarget(), $("#diagram").get(0));
  }
}