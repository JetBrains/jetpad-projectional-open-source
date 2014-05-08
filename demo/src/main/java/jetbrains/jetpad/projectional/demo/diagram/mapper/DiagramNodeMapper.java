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
package jetbrains.jetpad.projectional.demo.diagram.mapper;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNodeConnection;
import jetbrains.jetpad.projectional.diagram.util.SubList;
import jetbrains.jetpad.projectional.diagram.view.*;
import jetbrains.jetpad.projectional.view.RectView;
import jetbrains.jetpad.values.Color;

class DiagramNodeMapper extends Mapper<DiagramNode, RectView> {
  DiagramNodeMapper(DiagramNode source) {
    super(source, new RectView());

    getTarget().dimension().set(new Vector(50, 50));
    getTarget().focusable().set(true);

    getTarget().getProp(RootTrait.MOVE_HANDLER).set(new MoveHandler() {
      @Override
      public void move(Vector delta) {
        getSource().location.set(getSource().location.get().add(delta));
      }
    });

    getTarget().getProp(RootTrait.DELETE_HANDLER).set(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public void delete() {
        getSource().removeFromParent();
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().location, new WritableProperty<Vector>() {
      @Override
      public void set(Vector loc) {
        getTarget().moveTo(loc);
      }
    }));

    conf.add(Synchronizers.forObservableRole(this, getSource().connections,
      new SubList<Connection>() {
        @Override
        protected ObservableList<Connection> getBaseList() {
          return ((DiagramView) getParent().getTarget()).connections;
        }
      }, new MapperFactory<DiagramNodeConnection, Connection>() {
        @Override
        public Mapper<? extends DiagramNodeConnection, ? extends Connection> createMapper(DiagramNodeConnection source) {
          return new DiagramNodeConnectionMapper(source);
        }
      }));

    conf.add(Synchronizers.forProperty(
      Properties.ifProp(getTarget().focused(), Properties.constant(Color.RED), Properties.constant(Color.LIGHT_GRAY)),
      getTarget().background()
    ));
  }
}