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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagramExpr.model.SimpleDiagram;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.projectional.view.*;

public class RootSimpleDiagramMapper extends Mapper<SimpleDiagram, ViewContainer> {
  public RootSimpleDiagramMapper(SimpleDiagram source) {
    super(source, new ViewContainer());

    getTarget().root().addTrait(RootTrait.ROOT_TRAIT);

    final Value<Boolean> newItem = new Value<Boolean>(false);
    getTarget().root().addTrait(new ViewTraitBuilder()
      .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
        @Override
        public void handle(View view, KeyEvent e) {
          if (e.is(Key.N)) {
            newItem.set(true);
            e.consume();
          }

          if (e.is(Key.ESCAPE) && newItem.get()) {
            newItem.set(false);
            e.consume();
          }
        }
      })
      .on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          if (newItem.get()) {
            DiagramNode node = new DiagramNode();
            node.location.set(e.location());
            getSource().nodes.add(node);
            newItem.set(false);
            e.consume();
          }
        }
      })
      .build());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forConstantRole(this, getSource(), getTarget().contentRoot().children(), new MapperFactory<SimpleDiagram, View>() {
      @Override
      public Mapper<? extends SimpleDiagram, ? extends View> createMapper(SimpleDiagram source) {
        return new SimpleDiagramMapper(source);
      }
    }));
  }
}