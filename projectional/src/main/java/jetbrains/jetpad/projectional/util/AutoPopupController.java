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
package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class AutoPopupController {
  public static Registration install(final CellContainer container) {
    return container.focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
      private int eventCount;

      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        eventCount++;
        final Cell newFocus = event.getNewValue();
        final int currentEvent = eventCount;
        if (newFocus instanceof TextCell) {
          container.getEdt().schedule(1500, new Runnable() {
            @Override
            public void run() {
              if (eventCount == currentEvent && newFocus.get(Completion.COMPLETION_CONTROLLER) != null && !newFocus.get(Completion.COMPLETION_CONTROLLER).isActive()) {
                newFocus.get(Completion.COMPLETION_CONTROLLER).activate();
              }
            }
          });
        }
      }
    });
  }
}