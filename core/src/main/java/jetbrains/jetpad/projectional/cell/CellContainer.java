/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Objects;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.TextClipboardContent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.cell.event.CompletionEvent;

import java.util.*;

public class CellContainer {
  public static final State NULL_STATE = new State() {
    @Override
    public void restore() {
    }
  };

  public final Property<Cell> focusedCell;
  public final RootCell root = new RootCell(this);

  private List<Cell> myPopups = new ArrayList<Cell>();
  private Listeners<CellContainerListener> myListeners = new Listeners<CellContainerListener>();
  private boolean myDispatching;
  private CellContainerPeer myCellContainerPeer = CellContainerPeer.NULL;

  private String myLastSeenText;
  private ClipboardContent myContent = new EmptyClipboardContent();

  {
    focusedCell = new ValueProperty<Cell>() {
      @Override
      public void set(Cell value) {
        if (value != null) {
          if (!value.canFocus()) throw new IllegalStateException("cannot set focus: " + value);
          if (value.container() != CellContainer.this) throw new IllegalArgumentException();
        }

        Cell oldValue = get();
        if (oldValue == value) return;

        FocusEvent event =  new FocusEvent(oldValue, value);

        if (oldValue != null) {
          oldValue.set(Cell.FOCUSED, false);
        }
        super.set(value);
        if (oldValue != null) {
          dispatch(oldValue, event, CellEventSpec.FOCUS_LOST);
        }
        if (value != null) {
          dispatch(value, event, CellEventSpec.FOCUS_GAINED);
        }

        if (value != null) {
          value.set(Cell.FOCUSED, true);
        }
      }
    };
  }

  public State saveState() {
    final Cell oldCell = focusedCell.get();
    return new State() {
      @Override
      public void restore() {
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
    if ((e.is(Key.C, ModifierKey.CONTROL) || e.is(Key.C, ModifierKey.META))) {
      copy(new CopyCutEvent(false));
      e.consume();
      return;
    }

    if ((e.is(Key.X, ModifierKey.CONTROL) || e.is(Key.X, ModifierKey.META))) {
      cut(new CopyCutEvent(true));
      e.consume();
      return;
    }

    if ((e.is(Key.V, ModifierKey.CONTROL) || e.is(Key.V, ModifierKey.META))) {
      paste(new PasteEvent(myContent));
      e.consume();
      return;
    }

    if (e.is(Key.SPACE, ModifierKey.CONTROL)) {
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

  public void mousePressed(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_PRESSED);
  }

  public void mouseReleased(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_RELEASED);
  }

  public void mouseMoved(MouseEvent e) {
    mouseEventHappened(e, CellEventSpec.MOUSE_MOVED);
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
    Cell target = null;
    for (Cell popup : myPopups) {
      target = findCell(popup, e.location());
      if (target != null) break;
    }
    if (target == null) {
      target = findCell(root, e.location());
    }
    if (target == null) {
      target = root;
    }

    dispatch(target, e, eventSpec);
  }

  private Cell findCell(Cell current, Vector loc) {
    if (!current.getBounds().contains(loc)) return null;
    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;
      Cell result = findCell(child, loc);
      if (result != null) {
        return result;
      }
    }
    return current;
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

  /**
   * Executes a model related command. This method notifies CellContainerListener around the r.run() call.
   * For example, CellContainerListeners can save UI state, record undoable actions, and do other stuff.
   */
  public void executeCommand(Runnable r) {
    if (myDispatching) {
      r.run();
    } else {
      myDispatching = true;
      myListeners.fire(new ListenerCaller<CellContainerListener>() {
        @Override
        public void call(CellContainerListener l) {
          l.onBeforeInputEventHandled();
        }
      });
      try {
        r.run();
      } finally {
        myListeners.fire(new ListenerCaller<CellContainerListener>() {
          @Override
          public void call(CellContainerListener l) {
            l.onAfterInputEventHandled();
          }
        });
        myDispatching = false;
      }
    }
  }

  void viewAdded(Cell c) {
    for (Cell p : c.popups()) {
      myPopups.add(p);
    }
  }

  void viewRemoved(Cell c) {
    for (Cell p : c.popups()) {
      myPopups.remove(p);
    }
  }

  void viewPropertyChanged(final Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> change) {
    myListeners.fire(new ListenerCaller<CellContainerListener>() {
      @Override
      public void call(CellContainerListener l) {
        l.onViewPropertyChanged(cell, prop, change);
      }
    });
  }

  void viewChildAdded(final Cell cell, final CollectionItemEvent<Cell> change) {
    myListeners.fire(new ListenerCaller<CellContainerListener>() {
      @Override
      public void call(CellContainerListener l) {
        l.onChildAdded(cell, change);
      }
    });
  }

  void viewChildRemoved(final Cell cell, final CollectionItemEvent<Cell> change) {
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
    if (provider == null) throw new NullPointerException();
    myCellContainerPeer = provider;
  }

  public void resetContainerPeer() {
    myCellContainerPeer = CellContainerPeer.NULL;
  }

  public interface State {
    void restore();
  }
}