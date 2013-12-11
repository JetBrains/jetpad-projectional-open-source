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
package jetbrains.jetpad.projectional.cell.view;

import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.CellContainer;
import jetbrains.jetpad.projectional.cell.util.RootController;
import jetbrains.jetpad.projectional.view.HorizontalView;
import jetbrains.jetpad.projectional.view.View;

public class CellView extends HorizontalView {
  public final Property<Cell> cell = new ValueProperty<Cell>();

  private CellContainer myCellContainer = new CellContainer();


  public CellView(View popupView) {
    RootController.install(myCellContainer);
    cell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        myCellContainer.root.children().clear();
        if (event.getNewValue() != null) {
          myCellContainer.root.children().add(event.getNewValue());
        }
      }
    });

    MapperCell2View.map(myCellContainer, this, this, popupView);
  }
}