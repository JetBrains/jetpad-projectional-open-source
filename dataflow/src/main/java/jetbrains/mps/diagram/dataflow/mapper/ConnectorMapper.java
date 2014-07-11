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
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.cell.view.CellView;
import jetbrains.jetpad.projectional.diagram.view.DeleteHandler;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.mps.diagram.dataflow.model.Connector;
import jetbrains.mps.diagram.dataflow.model.Diagram;
import jetbrains.mps.diagram.dataflow.model.InputPort;
import jetbrains.mps.diagram.dataflow.model.OutputPort;

public class ConnectorMapper extends Mapper<Connector, PolyLineConnection> {
  ConnectorMapper(Connector source, View popupView) {
    super(source, new PolyLineConnection(new CellView()));

    getTarget().view().getProp(RootTrait.DELETE_HANDLER).set(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public void delete() {
        Diagram diagram = (Diagram) getParent().getSource();
        diagram.connectors.remove(getSource());
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().input, new WritableProperty<InputPort>() {
      @Override
      public void set(InputPort value) {
        getTarget().toView().set(value == null ? null : (View) getParent().getDescendantMapper(value).getTarget());
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().output, new WritableProperty<OutputPort>() {
      @Override
      public void set(OutputPort value) {
        getTarget().fromView().set(value == null ? null : (View) getParent().getDescendantMapper(value).getTarget());
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().inputLocation, getTarget().toLocation()));
  }
}