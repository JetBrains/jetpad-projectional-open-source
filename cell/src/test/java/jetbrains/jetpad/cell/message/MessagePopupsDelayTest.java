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
package jetbrains.jetpad.cell.message;

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.TestEventDispatchThread;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.event.KeyStroke;
import jetbrains.jetpad.event.KeyStrokeSpec;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.ReadableProperty;
import org.junit.Test;

public class MessagePopupsDelayTest extends MessageControllerTestCase {
  private TestEventDispatchThread edt = new TestEventDispatchThread();
  private long myCurrentTime = 0;

  @Test
  public void delayOnKeyTyped() {
    delayTest(new Runnable() {
      @Override
      public void run() {
        type('a');
      }
    });
  }

  @Test
  public void delayOnBackspace() {
    delayTest(new Runnable() {
      @Override
      public void run() {
        backspace();
      }
    });
  }

  @Test
  public void delayOnDelete() {
    delayTest(new Runnable() {
      @Override
      public void run() {
        del();
      }
    });
  }

  @Test
  public void delayOnDeleteCurrent() {
    KeyStrokeSpec spec = KeyStrokeSpecs.DELETE_CURRENT;
    for (final KeyStroke keyStroke : spec.getKeyStrokes()) {
      delayTest(new Runnable() {
        @Override
        public void run() {
          press(keyStroke);
        }
      });
    }
  }

  @Test
  public void delayOnSpace() {
    delayTest(new Runnable() {
      @Override
      public void run() {
        type(' ');
      }
    });
  }

  private void delayTest(Runnable action) {
    showErrorPopup();
    cell.focus();

    action.run();
    assertDecorationPopupVisible(cell, false);

    myCurrentTime += MessageTrait.POPUPS_SHOW_DELAY_MILLIS;
    edt.executeUpdates(MessageTrait.POPUPS_SHOW_DELAY_MILLIS);
    assertDecorationPopupVisible(cell, true);
  }

  @Test
  public void delayAffectsAllMessagesPopups() {
    myCellContainer.root.children().clear();
    HorizontalCell parent = new HorizontalCell();
    parent.children().add(cell);
    Cell cell2 = doCreateCell();
    parent.children().add(cell2);
    myCellContainer.root.children().add(parent);

    setError(cell2);
    cell2.focus();
    help();
    assertDecorationPopupVisible(cell2, true);

    showErrorPopup();

    type('a');

    assertDecorationPopupVisible(cell, false);
    assertDecorationPopupVisible(cell2, false);

    myCurrentTime += MessageTrait.POPUPS_SHOW_DELAY_MILLIS;
    edt.executeUpdates(MessageTrait.POPUPS_SHOW_DELAY_MILLIS);
    assertDecorationPopupVisible(cell, true);
    assertDecorationPopupVisible(cell2, true);
  }

  @Override
  protected Cell doCreateCell() {
    TextCell c = new TextCell("abcdefghijklmnopqrstuvwxyz");
    c.focusable().set(true);
    c.addTrait(TextEditing.textEditing());
    return c;
  }

  @Override
  protected Registration installMessageController(CellContainer container) {
    return MessageController.install(container, null, new Supplier<Long>() {
      @Override
      public Long get() {
        return myCurrentTime;
      }
    });
  }

  @Override
  public void init() {
    final CellContainerPeer peer = getCellContainerPeer();
    myCellContainer.setCellContainerPeer(new CellContainerPeer() {
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

    super.init();
  }
}
