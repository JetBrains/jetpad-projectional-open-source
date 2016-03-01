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
package jetbrains.jetpad.cell.position;

import jetbrains.jetpad.cell.Cell;

final class CellDefaultPositionHandler extends DefaultPositionHandler {

  private final Cell myCell;

  CellDefaultPositionHandler(Cell cell) {
    myCell = cell;
  }

  @Override
  public boolean isHome() {
    Cell childCell = myCell.firstChild();
    return childCell == null ? super.isHome() : childCell.get(PositionHandler.PROPERTY).isHome();
  }

  @Override
  public boolean isEnd() {
    Cell childCell = myCell.lastChild();
    return childCell == null ? super.isEnd() : childCell.get(PositionHandler.PROPERTY).isEnd();
  }

  @Override
  public boolean isEmpty() {
    boolean empty = true;
    for (Cell child : myCell.children()) {
      if (!child.get(PositionHandler.PROPERTY).isEmpty()) {
        empty = false;
        break;
      }
    }
    return empty;
  }

}
