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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.view.ViewCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;

class ViewCellMapper extends BaseCellMapper<ViewCell> {
  private ViewContainer myViewContainer;

  ViewCellMapper(ViewCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
    myViewContainer = new ViewContainer();
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forRegistration(ViewToDom.map(myViewContainer, getTarget())));

    conf.add(Synchronizers.forProperty(getSource().view, new WritableProperty<View>() {
      @Override
      public void set(View value) {
        myViewContainer.contentRoot().children().clear();

        if (value != null) {
          myViewContainer.contentRoot().children().add(value);
        }
      }
    }));
  }
}