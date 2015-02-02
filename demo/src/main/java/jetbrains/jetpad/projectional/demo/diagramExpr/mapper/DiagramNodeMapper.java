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
package jetbrains.jetpad.projectional.demo.diagramExpr.mapper;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNodeConnection;
import jetbrains.jetpad.projectional.demo.hybridExpr.mapper.ExprHybridPositionSpec;
import jetbrains.jetpad.projectional.diagram.view.*;
import jetbrains.jetpad.values.Color;

class DiagramNodeMapper extends Mapper<DiagramNode, DiagramNodeView> {
  DiagramNodeMapper(DiagramNode source) {
    super(source, new DiagramNodeView());

    getTarget().padding().set(10);
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

    conf.add(Synchronizers.forPropsOneWay(getSource().location, new WritableProperty<Vector>() {
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

    conf.add(Synchronizers.forPropsOneWay(
      Properties.ifProp(getTarget().focused(), Color.RED, Color.LIGHT_GRAY),
      getTarget().background()
    ));

    HorizontalCell cell = new HorizontalCell();
    getTarget().cellView.cell.set(cell);
    conf.add(new HybridSynchronizer<>(this, getSource().expression, cell, new ExprHybridPositionSpec()));
  }
}