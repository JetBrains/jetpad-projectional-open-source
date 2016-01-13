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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.ReadableProperty;

public class CellContainerEdtUtil {

  public static void resetEdt(final CellContainer container, final EventDispatchThread edt) {
    final CellContainerPeer peer = container.getCellContainerPeer();
    container.setCellContainerPeer(new CellContainerPeer() {
      @Override
      public int getCaretAt(TextCell tv, int x) {
        return peer.getCaretAt(tv, x);
      }

      @Override
      public int getCaretOffset(TextCell tv, int caret) {
        return peer.getCaretOffset(tv, caret);
      }

      @Override
      public Rectangle getBounds(Cell cell) {
        return peer.getBounds(cell);
      }

      @Override
      public void scrollTo(Rectangle rect, Cell cell) {
        peer.scrollTo(rect, cell);
      }

      @Override
      public Cell findCell(Cell root, Vector loc) {
        return peer.findCell(root, loc);
      }

      @Override
      public ReadableProperty<Boolean> focused() {
        return peer.focused();
      }

      @Override
      public Rectangle visibleRect() {
        return peer.visibleRect();
      }

      @Override
      public void requestFocus() {
        peer.requestFocus();
      }

      @Override
      public EventDispatchThread getEdt() {
        return edt;
      }
    });
  }
}
