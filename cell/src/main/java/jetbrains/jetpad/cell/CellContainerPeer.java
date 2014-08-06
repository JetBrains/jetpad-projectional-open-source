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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.base.animation.Animations;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.NullEventDispatchThread;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;

public interface CellContainerPeer {
  public static final CellContainerPeer NULL = new CellContainerPeer() {
    @Override
    public int getCaretAt(TextCell tv, int x) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getCaretOffset(TextCell tv, int caret) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle getBounds(Cell cell) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void scrollTo(Rectangle rect, Cell cell) {
    }


    @Override
    public Cell findCell(Cell root, Vector loc) {
      return Cells.findCell(root, loc);
    }

    @Override
    public Rectangle visibleRect() {
      return new Rectangle(0, 0, 10000, 10000);
    }

    @Override
    public void requestFocus() {
    }

    @Override
    public ReadableProperty<Boolean> focused() {
      return Properties.constant(true);
    }

    @Override
    public EventDispatchThread getEdt() {
      return new NullEventDispatchThread();
    }

    @Override
    public Animation fadeIn(Cell cell, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation fadeOut(Cell cell, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation showSlide(Cell cell, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation hideSlide(Cell cell, int duration) {
      return Animations.finishedAnimation();
    }
  };


  int getCaretAt(TextCell tv, int x);
  int getCaretOffset(TextCell tv, int caret);
  Rectangle getBounds(Cell cell);
  void scrollTo(Rectangle rect, Cell cell);

  Cell findCell(Cell root, Vector loc);

  ReadableProperty<Boolean> focused();
  Rectangle visibleRect();
  void requestFocus();

  EventDispatchThread getEdt();

  Animation fadeIn(Cell cell, int duration);
  Animation fadeOut(Cell cell, int duration);
  Animation showSlide(Cell cell, int duration);
  Animation hideSlide(Cell cell, int duration);
}