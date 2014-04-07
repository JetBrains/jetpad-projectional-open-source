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

import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.NullEventDispatchThread;
import jetbrains.jetpad.geometry.Rectangle;
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
    public void scrollTo(Cell cell) {
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
  };

  int getCaretAt(TextCell tv, int x);
  int getCaretOffset(TextCell tv, int caret);
  Rectangle getBounds(Cell cell);
  void scrollTo(Cell cell);
  ReadableProperty<Boolean> focused();

  void requestFocus();

  EventDispatchThread getEdt();
}