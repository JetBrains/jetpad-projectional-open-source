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
package jetbrains.mps.diagram.dataflow.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.diagram.layout.OrthogonalRouter;
import jetbrains.jetpad.projectional.diagram.view.Connection;
import jetbrains.jetpad.projectional.diagram.view.ConnectionRoutingView;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.diagram.view.decoration.ConnectionDivergeDecoration;
import jetbrains.jetpad.projectional.diagram.view.decoration.DecorationContainer;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.mps.diagram.dataflow.model.Block;
import jetbrains.mps.diagram.dataflow.model.Connector;
import jetbrains.mps.diagram.dataflow.model.Diagram;
import jetbrains.mps.diagram.dataflow.view.LabelsView;

public class DiagramMapper extends Mapper<Diagram, DiagramView> {
  LabelsView myLabelsView;
  MapperFactory<Block, View> myBlockMapperFactory;

  public DiagramMapper(Diagram source, MapperFactory<Block, View> blockMapperFactory) {
    super(source, createTarget());
    myLabelsView = new LabelsView((DecorationContainer) getTarget());
    myBlockMapperFactory = blockMapperFactory;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().blocks, getTarget().itemsView.children(), myBlockMapperFactory));
    conf.add(Synchronizers.forObservableRole(this, getSource().connectors, getTarget().connections, new MapperFactory<Connector, Connection>() {
      @Override
      public Mapper<? extends Connector, ? extends Connection> createMapper(Connector source) {
        return new ConnectorMapper(source, getTarget().popupView);
      }
    }));
    conf.add(Synchronizers.forObservableRole(this, getSource().connectors, myLabelsView.children(), new MapperFactory<Connector, View>() {
      @Override
      public Mapper<? extends Connector, ? extends View> createMapper(Connector source) {
        int index = getSource().connectors.indexOf(source);
        return new LabelMapper(source, getTarget().popupView, (PolyLineConnection) getTarget().connections.get(index), getTarget());
      }
    }));
  }

  private static DiagramView createTarget() {
    ConnectionRoutingView view = new ConnectionRoutingView(new OrthogonalRouter());
    new ConnectionDivergeDecoration(view);
    return view;
  }
}