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

import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.diagram.view.DeleteHandler;
import jetbrains.jetpad.projectional.diagram.view.MoveHandler;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewEventHandler;
import jetbrains.jetpad.projectional.view.ViewEvents;
import jetbrains.jetpad.projectional.view.ViewTraitBuilder;
import jetbrains.jetpad.values.Color;
import jetbrains.mps.diagram.dataflow.model.*;
import jetbrains.mps.diagram.dataflow.view.BlockView;

import java.util.Iterator;

public class BlockMapper extends Mapper<Block, BlockView> {
  private static int ourBlockCounter = 0;

  public BlockMapper(Block source) {
    super(source, new BlockView());

    getSource().text.set("block " + ourBlockCounter++);

    getTarget().focusable().set(true);
    getTarget().padding().set(10);

    getTarget().addTrait(new ViewTraitBuilder().on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (getTarget().focused().get() && e.is(Key.T)) {
          getTarget().setPortsDirection(getTarget().getPortsDirection().turnClockwise());
        }
      }
    }).build());
    getTarget().rect.prop(RootTrait.MOVE_HANDLER).set(new MoveHandler() {
      @Override
      public void move(Vector delta) {
        getSource().location.set(getSource().location.get().add(delta));
      }
    });
    getTarget().prop(RootTrait.DELETE_HANDLER).set(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public void delete() {
        Diagram diagram = (Diagram) getParent().getSource();
        for (Iterator<Connector> it = diagram.connectors.iterator(); it.hasNext(); ) {
          Connector c = it.next();
          if (c.input.get().parent().get() == getSource() || c.output.get().parent().get() == getSource()) {
            it.remove();
          }
        }
        diagram.blocks.remove(getSource());
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(Properties.ifProp(getTarget().focused(), Properties.constant(Color.RED), getSource().color), getTarget().rect.background()));
    conf.add(Synchronizers.forProperty(getSource().location, new WritableProperty<Vector>() {
      @Override
      public void set(Vector value) {
        getTarget().moveTo(value);
        getTarget().invalidate();
      }
    }));
    conf.add(Synchronizers.forObservableRole(this, getSource().inputs, getTarget().inputs.children(), new MapperFactory<InputPort, View>() {
      @Override
      public Mapper<? extends InputPort, ? extends View> createMapper(InputPort source) {
        return new InputPortMapper(source);
      }
    }));
    conf.add(Synchronizers.forObservableRole(this, getSource().outputs, getTarget().outputs.children(), new MapperFactory<OutputPort, View>() {
      @Override
      public Mapper<? extends OutputPort, ? extends View> createMapper(OutputPort source) {
        return new OutputPortMapper(source);
      }
    }));
  }
}