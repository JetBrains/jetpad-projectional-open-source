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
package jetbrains.mps.diagram.dataflow.mapper;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.mps.diagram.dataflow.model.Block;
import jetbrains.mps.diagram.dataflow.model.Blocks;
import jetbrains.mps.diagram.dataflow.model.Diagram;

public class RootDiagramMapper extends Mapper<Diagram, ViewContainer> {
  public RootDiagramMapper(Diagram source) {
    super(source, new ViewContainer());
    initRoot(getSource(), getTarget());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forConstantRole(this, getSource(), getTarget().contentRoot().children(), new MapperFactory<Diagram, View>() {
      @Override
      public Mapper<? extends Diagram, ? extends View> createMapper(Diagram source) {
        return new DiagramMapper(source, new MapperFactory<Block, View>() {
          @Override
          public Mapper<? extends Block, ? extends View> createMapper(Block source) {
            return new BlockWithLabelMapper(source);
          }
        });
      }
    }));
  }

  public static void initRoot(final Diagram diagram, final ViewContainer container) {
    container.root().addTrait(RootTrait.ROOT_TRAIT);

    final Value<Block> newBlock = new Value<>(null);
    container.root().addTrait(new ViewTraitBuilder().on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (container.focusedView().get() != null) {
          return;
        }

        if (e.is(Key.C)) {
          newBlock.set(Blocks.newConstant());
          e.consume();
        }

        if (e.is(Key.I)) {
          newBlock.set(Blocks.newInt());
          e.consume();
        }

        if (e.is(Key.S)) {
          newBlock.set(Blocks.newSum());
          e.consume();
        }

        if (e.is(Key.O)) {
          newBlock.set(Blocks.newOutput());
          e.consume();
        }

      }
    }).on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        if (newBlock.get() != null) {
          newBlock.get().location.set(e.getLocation());
          diagram.blocks.add(newBlock.get());
          newBlock.set(null);
          e.consume();
        }
      }
    }).build());
  }
}