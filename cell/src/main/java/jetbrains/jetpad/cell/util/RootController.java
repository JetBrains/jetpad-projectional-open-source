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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class RootController {
  public static Registration install(CellContainer container) {
    return new CompositeRegistration(
      NavigationController.install(container),
      container.focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Cell> event) {
          Cell oldCell = event.getOldValue();
          if (oldCell != null) {
            oldCell.highlighted().set(false);
          }

          Cell newCell = event.getNewValue();
          if (newCell != null) {
            newCell.highlighted().set(true);
          }
        }
      })
    );
  }
}