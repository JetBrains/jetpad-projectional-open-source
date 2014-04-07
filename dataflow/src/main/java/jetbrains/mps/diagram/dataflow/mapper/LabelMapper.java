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

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.MoveHandler;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.diagram.view.RootTrait;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.mps.diagram.dataflow.model.Connector;
import jetbrains.mps.diagram.dataflow.view.LabelView;

class LabelMapper extends Mapper<Connector, LabelView> {
  private static int ourConnectorCounter = 0;

  LabelMapper(Connector source, View popupView, PolyLineConnection connection, DiagramView diagramView) {
    super(source, new LabelView(connection, diagramView));

    getSource().text.set("label " + ourConnectorCounter++);

    getTarget().prop(RootTrait.MOVE_HANDLER).set(new MoveHandler() {
      @Override
      public void move(Vector delta) {
        getTarget().changeLabelDelta(delta);
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(new Synchronizer() {
      Registration myReg;
      Synchronizer myPropSynchronizer;
      Synchronizer myEditingSynchronizer;
      Synchronizer myInvalidateSynchronizer;

      @Override
      public void attach(SynchronizerContext ctx) {
        TextCellController textCellController = new TextCellController();
        myReg = textCellController.install(getTarget());
        myPropSynchronizer = Synchronizers.forProperties(getSource().text, textCellController.getTextCell().text());
        myEditingSynchronizer = Synchronizers.forProperties(textCellController.editing(), getTarget().editing());
        myInvalidateSynchronizer = Synchronizers.forProperty(textCellController.editing(), new Runnable() {
          @Override
          public void run() {
            getTarget().invalidate();
          }
        });

        myPropSynchronizer.attach(ctx);
        myEditingSynchronizer.attach(ctx);
        myInvalidateSynchronizer.attach(ctx);
      }

      @Override
      public void detach() {
        myPropSynchronizer.detach();
        myEditingSynchronizer.detach();
        myInvalidateSynchronizer.detach();

        myReg.remove();
      }
    });
  }
}