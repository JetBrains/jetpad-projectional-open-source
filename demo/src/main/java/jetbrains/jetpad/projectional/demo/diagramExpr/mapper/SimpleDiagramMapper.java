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
package jetbrains.jetpad.projectional.demo.diagramExpr.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramConnection;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.SimpleDiagram;
import jetbrains.jetpad.projectional.diagram.view.Connection;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.view.View;

class SimpleDiagramMapper extends Mapper<SimpleDiagram, DiagramView> {
  SimpleDiagramMapper(SimpleDiagram source) {
    super(source, new DiagramView());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().nodes, getTarget().itemsView.children(), new MapperFactory<DiagramNode, View>() {
      @Override
      public Mapper<? extends DiagramNode, ? extends View> createMapper(DiagramNode source) {
        return new DiagramNodeMapper(source, getTarget().popupView);
      }
    }));

    conf.add(Synchronizers.forObservableRole(this,
      getSource().connections,
      new SubList<Connection>() {
        @Override
        protected ObservableList<Connection> getBaseList() {
          return getTarget().connections;
        }
      },
      new MapperFactory<DiagramConnection, Connection>() {
        @Override
        public Mapper<? extends DiagramConnection, ? extends Connection> createMapper(DiagramConnection source) {
          return new DiagramConnectionMapper(source);
        }
      }));
  }
}