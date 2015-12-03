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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.geometry.Rectangle;

public abstract class PopupPositionUpdater<ViewT> {

  public final void update(CellPropertySpec<Cell> kind,  ViewT popup, Rectangle target) {
    if (Cell.BOTTOM_POPUP == kind) {
      updateBottom(target, popup);
    } else if (Cell.FRONT_POPUP == kind) {
      updateFront(target, popup);
    } else if (Cell.RIGHT_POPUP == kind) {
      updateRight(target, popup);
    } else if (Cell.LEFT_POPUP == kind) {
      updateLeft(target, popup);
    }
  }

  protected abstract void updateLeft(Rectangle target, ViewT popup);
  protected abstract void updateRight(Rectangle target, ViewT popup);
  protected abstract void updateFront(Rectangle target, ViewT popup);
  protected abstract void updateBottom(Rectangle target, ViewT popup);
}
