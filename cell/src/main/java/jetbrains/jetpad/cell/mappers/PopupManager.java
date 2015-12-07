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
package jetbrains.jetpad.cell.mappers;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public interface PopupManager extends EventHandler<PropertyChangeEvent<Cell>>, Disposable {
  void attach(Cell cell);
  void updatePopupPositions();

  PopupManager EMPTY = new PopupManager() {
    @Override
    public void attach(Cell cell) {
    }

    @Override
    public void updatePopupPositions() {
    }

    @Override
    public void onEvent(PropertyChangeEvent<Cell> event) {
    }

    @Override
    public void dispose() {
    }
  };
}
