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

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.cell.event.CellEventSpec;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.ArrayList;
import java.util.List;

public class CellContainer {
  public final Property<Cell> focusedCell;
  public final RootCell root = new RootCell(this);

  private Cell myCellUnderMouse;
  private Cell myDragStart;

  private List<Cell> myPopups = new ArrayList<>();
  private Listeners<CellContainerListener> myListeners = new Listeners<>();
  private boolean myInCommand;
  private CellContainerPeer myCellContainerPeer = CellContainerPeer.NULL;

  private String myLastSeenText;
  private ClipboardContent myContent = new EmptyClipboardContent();

  public CellContainer() {
    focusedCell = new ValueProperty<Cell>() {
      private boolean mySettingValue;

      @Override
      public void set(Cell value) {
        if (mySettingValue) {
          throw new IllegalStateException("You can't change focus in focusCell change handler");
        }

        mySettingValue = true;
        try {
          if (value != null) {
            if (!value.canFocus()) {
              throw new IllegalStateException("Cannot set focus to " + value);
            }
            if (value.getContainer() != CellContainer.this) {
              throw new IllegalArgumentException();
            }
          }

          Cell oldValue = get();
          if (oldValue == value) return;

          if (oldValue != null) {
            oldValue.set(Cell.FOCUSED, false);
          }
          super.set(value);
          FocusEvent event = new FocusEvent(oldValue, value);
          if (oldValue != null) {
            dispatch(oldValue, event, CellEventSpec.FOCUS_LOST);
          }
          if (value != null) {
            dispatch(value, event, CellEventSpec.FOCUS_GAINED);
            value.set(Cell.FOCUSED, true);
          }
        } finally {
          mySettingValue = false;
        }
      }
    };
  }

  public Runnable saveState() {
    final Cell oldCell = focusedCell.get();

    return new Runnable() {
      @Override
      public void run() {
        if (oldCell != null) {
          if (!oldCell.canFocus()) {
            throw new IllegalStateException();
          }
          oldCell.focus();
        } else {
          focusedCell.set(null);
        }
      }
    };
  }

  public void keyPressed(KeyEvent e) {
    if (e.is(KeyStrokeSpecs.COPY)) {
      copy(new CopyCutEvent(false));
      e.consume();
      return;
    }

    if (e.is(KeyStrokeSpecs.CUT)) {
      cut(new CopyCutEvent(true));
      e.consume();
      return;
    }

    if (e.is(KeyStrokeSpecs.PASTE)) {
      paste(new PasteEvent(myContent));
      e.consume();
      return;
    }

    if (e.is(KeyStrokeSpecs.COMPLETE)) {
      CompletionEvent event = new CompletionEvent(false);
      complete(event);
      if (event.isConsumed()) {
        e.consume();
        return;
      }
    }

    dispatch(e, CellEventSpec.KEY_PRESSED);
  }

  public void keyReleased(KeyEvent e) {
    dispatch(e, CellEventSpec.KEY_RELEASED);
  }

  public void keyTyped(KeyEvent e) {
    dispatch(e, CellEventSpec.KEY_TYPED);
  }

  public void mouseClicked(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_CLICKED);
  }

  public void mousePressed(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_PRESSED);
  }

  public void mouseReleased(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_RELEASED);
  }

  public void mouseMoved(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_MOVED);
    changeCellUnderMouse(e, findCell(e.getLocation()));
  }

  public void mouseDragged(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_DRAGGED);
    changeCellUnderMouse(e, findCell(e.getLocation()));
  }

  public void mouseEntered(MouseEvent e) {
    changeCellUnderMouse(e, findCell(e.getLocation()));
  }

  public void mouseLeft(MouseEvent e) {
    changeCellUnderMouse(e, null);
  }

  private void changeCellUnderMouse(MouseEvent e, Cell newCell) {
    if (newCell == myCellUnderMouse) return;

    if (myCellUnderMouse != null) {
      dispatch(myCellUnderMouse, new MouseEvent(e.getLocation()), CellEventSpec.MOUSE_LEFT);
    }

    if (newCell != null) {
      dispatch(newCell, new MouseEvent(e.getLocation()), CellEventSpec.MOUSE_ENTERED);
    }

    myCellUnderMouse = newCell;
  }


  public void copy(CopyCutEvent e) {
    dispatch(e, CellEventSpec.COPY);
    if (e.isConsumed()) {
      setContent(e);
    }
  }

  public void cut(CopyCutEvent e) {
    dispatch(e, CellEventSpec.CUT);
    if (e.isConsumed()) {
      setContent(e);
    }
  }

  public void paste(PasteEvent e) {
    dispatch(e, CellEventSpec.PASTE);
  }

  public void complete(CompletionEvent e) {
    dispatch(e, CellEventSpec.COMPLETE);
  }

  private void setContent(CopyCutEvent e) {
    myContent = e.getResult();
    if (myContent.isSupported(ContentKinds.TEXT)) {
      myLastSeenText = myContent.get(ContentKinds.TEXT);
    } else {
      myLastSeenText = myContent.toString();
    }
  }

  public void paste(String text) {
    if (!Objects.equal(myLastSeenText, text)) {
      myContent = new TextClipboardContent(text);
      myLastSeenText = text;
    }

    dispatch(new PasteEvent(myContent), CellEventSpec.PASTE);
  }

  private void mouseEventHappened(MouseEvent e, CellEventSpec<MouseEvent> eventSpec) {
    Cell target = findCell(e.getLocation());

    if (eventSpec == CellEventSpec.MOUSE_PRESSED) {
      myDragStart = target;
    }

    if (eventSpec == CellEventSpec.MOUSE_DRAGGED && myDragStart != null) {
      dispatch(myDragStart, e, eventSpec);
    } else {
      dispatch(target, e, eventSpec);
    }
  }

  public Cell findCell(Cell current, Vector loc) {
    return getCellContainerPeer().findCell(current, loc);
  }

  public Cell findCell(Vector loc) {
    Cell target = null;
    for (Cell popup : myPopups) {
      target = findCell(popup, loc);
      if (target != null) break;
    }
    if (target == null) {
      target = findCell(root, loc);
    }
    if (target == null) {
      target = root;
    }
    return target;
  }

  private <EventT extends Event> void dispatch(final EventT e, final CellEventSpec<EventT> spec) {
    executeCommand(new Runnable() {
      @Override
      public void run() {
        dispatch(focusedCell.get(), e, spec);
      }
    });
  }

  private <EventT extends Event> void dispatch(final Cell target, final EventT e, final CellEventSpec<EventT> spec) {
    if (target == null) return;
    executeCommand(new Runnable() {
      @Override
      public void run() {
        target.dispatch(e, spec);
      }
    });
  }

  public boolean isInCommand() {
    return myInCommand;
  }

  /**
   * Executes a model related command. This method notifies CellContainerListener around the r.run() call.
   * For example, CellContainerListeners can save UI state, record undoable actions, and do other stuff.
   */
  public void executeCommand(Runnable r) {
    if (myInCommand) {
      r.run();
    } else {
      myInCommand = true;
      myListeners.fire(new ListenerCaller<CellContainerListener>() {
        @Override
        public void call(CellContainerListener l) {
          l.onBeforeCommand();
        }
      });
      try {
        r.run();
      } finally {
        myListeners.fire(new ListenerCaller<CellContainerListener>() {
          @Override
          public void call(CellContainerListener l) {
            l.onAfterCommand();
          }
        });
        myInCommand = false;
      }
    }
  }

  void cellAdded(Cell c) {
    for (Cell p : c.popups()) {
      myPopups.add(p);
    }
  }

  void cellRemoved(Cell c) {
    for (Cell p : c.popups()) {
      myPopups.remove(p);
    }
  }

  void cellPropertyChanged(final Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> change) {
    myListeners.fire(new ListenerCaller<CellContainerListener>() {
      @Override
      public void call(CellContainerListener l) {
        l.onCellPropertyChanged(cell, prop, change);
      }
    });
  }

  void cellChildAdded(final Cell cell, final CollectionItemEvent<? extends Cell> change) {
    myListeners.fire(new ListenerCaller<CellContainerListener>() {
      @Override
      public void call(CellContainerListener l) {
        l.onChildAdded(cell, change);
      }
    });
  }

  void cellChildRemoved(final Cell cell, final CollectionItemEvent<? extends Cell> change) {
    myListeners.fire(new ListenerCaller<CellContainerListener>() {
      @Override
      public void call(CellContainerListener l) {
        l.onChildRemoved(cell, change);
      }
    });
  }

  void popupAdded(Cell c) {
    myPopups.add(c);
  }

  void popupRemoved(Cell c) {
    myPopups.remove(c);
  }

  public Registration addListener(CellContainerListener l) {
    return myListeners.add(l);
  }

  CellContainerPeer getCellContainerPeer() {
    return myCellContainerPeer;
  }

  public void setCellContainerPeer(CellContainerPeer provider) {
    if (provider == null) {
      throw new NullPointerException();
    }
    myCellContainerPeer = provider;
  }

  public void resetContainerPeer() {
    myCellContainerPeer = CellContainerPeer.NULL;
  }

  public ReadableProperty<Boolean> focused() {
    return getCellContainerPeer().focused();
  }

  public void requestFocus() {
    getCellContainerPeer().requestFocus();
  }

  public Rectangle visibleRect() {
    return getCellContainerPeer().visibleRect();
  }


  public EventDispatchThread getEdt() {
    return getCellContainerPeer().getEdt();
  }
}