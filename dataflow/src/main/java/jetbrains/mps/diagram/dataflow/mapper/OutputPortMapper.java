/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.RectView;
import jetbrains.jetpad.values.Color;
import jetbrains.mps.diagram.dataflow.model.Connector;
import jetbrains.mps.diagram.dataflow.model.OutputPort;

public class OutputPortMapper extends Mapper<OutputPort, RectView> {
  OutputPortMapper(OutputPort source) {
    super(source, new RectView());
    getTarget().dimension().set(new Vector(10, 10));
    getTarget().background().set(Color.GRAY);
    getTarget().focusable().set(true);

    final Value<Connector> connector = new Value<>();
    getTarget().addTrait(new ViewTraitBuilder().on(ViewEvents.MOUSE_DRAGGED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        if (connector.get() == null) {
          Connector newConnector = new Connector();
          newConnector.output.set(getSource());
          newConnector.inputLocation.set(e.getLocation());
          getSource().parent().get().parent().get().connectors.add(newConnector);
          connector.set(newConnector);
        } else {
          connector.get().inputLocation.set(e.getLocation());
        }
      }
    }).on(ViewEvents.MOUSE_RELEASED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        if (connector.get() == null) return;
        View atEvent = getTarget().container().root().viewAt(e.getLocation());
        if (atEvent == null || atEvent.getProp(InputPortMapper.PORT).get() == null) {
          connector.get().removeFromParent();
        } else {
          connector.get().input.set(atEvent.getProp(InputPortMapper.PORT).get());
        }
        connector.set(null);
      }
    }).build());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
  }
}