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
package jetbrains.jetpad.cell.decorations;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class Popups {

  public static void updatePopups(Cell c, EventHandler<PropertyChangeEvent<Cell>> handler) {
    for (CellPropertySpec<Cell> ps : Cell.POPUP_SPECS) {
      Cell popup = c.get(ps);
      if (popup != null) {
        handler.onEvent(new PropertyChangeEvent<>(null, popup));
      }
    }
  }

  public static <ViewT> void updatePopupsPositions(Cell c, PopupPositionUpdater<ViewT> p,
                                                   Mapper<? extends Cell, ? extends ViewT> m) {
    for (CellPropertySpec<Cell> ps : Cell.POPUP_SPECS) {
      Cell popup = c.get(ps);
      if (popup != null) {
        Mapper<?, ? extends ViewT> popupMapper = (Mapper<?, ? extends ViewT>) m.getDescendantMapper(popup);
        p.update(ps, popupMapper.getTarget(), c.getBounds());
      }
    }
  }
}
