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

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNode;
import jetbrains.jetpad.projectional.demo.diagram.model.DiagramNodeConnection;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.diagram.view.DeleteHandler;
import jetbrains.jetpad.projectional.diagram.view.LineConnection;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.values.Color;

class DiagramNodeConnectionMapper extends Mapper<DiagramNodeConnection, LineConnection> {
  DiagramNodeConnectionMapper(DiagramNodeConnection source) {
    super(source, new LineConnection());

    getTarget().view().focusable().set(true);
    getTarget().view().prop(RootTrait.DELETE_HANDLER).set(new DeleteHandler() {
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
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    getTarget().start().set((View) getParent().getTarget());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().target, new WritableProperty<DiagramNode>() {
      @Override
      public void set(DiagramNode value) {
        getTarget().end().set((View) getParent().getParent().getDescendantMapper(value).getTarget());
      }
    }));

    conf.add(Synchronizers.forProperty(
      Properties.ifProp(getTarget().view().focused(), Properties.constant(Color.RED), Properties.constant(Color.DARK_BLUE)),
      getTarget().color()
    ));
  }
}