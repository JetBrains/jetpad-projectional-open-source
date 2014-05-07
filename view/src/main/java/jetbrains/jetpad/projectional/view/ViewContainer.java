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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;

import java.util.ArrayList;
import java.util.List;

public class ViewContainer {
  private ViewContainerPeer myPeer = new NullViewContainerPeer();
  private RootView myContentRoot = new RootView();
  private Property<View> myFocusedView = new FocusedViewProperty();
  private View myDragStart;
  private List<Runnable> myOnValidate = new ArrayList<>();
  private Listeners<ViewContainerListener> myListeners = new Listeners<>();
  private boolean myInCommand;
  private View myViewUnderMouse;

  public ViewContainer() {
    myPeer.attach(this);
    myContentRoot.attach(this);
  }

  public Property<View> focusedView() {
    return myFocusedView;
  }

  public View contentRoot() {
    return myContentRoot.myContentRoot;
  }

  public View decorationRoot() {
    return myContentRoot.myDecorationView;
  }

  public View root() {
    return myContentRoot;
  }

  public void whenValid(Runnable r) {
    if (myContentRoot.valid().get()) {
      r.run();
    } else {
      myOnValidate.add(r);
    }
  }

  public Rectangle visibleRect() {
    return myPeer.visibleRect();
  }

  public void setPeer(ViewContainerPeer peer) {
    myPeer.detach();
    myPeer = peer;
    myContentRoot.invalidateTree();
    myPeer.attach(this);
  }

  ViewContainerPeer peer() {
    return myPeer;
  }

  void repaint(View view) {
    myPeer.repaint(view);
  }

  void boundsChanged(View view, PropertyChangeEvent<Rectangle> change) {
    myPeer.boundsChanged(view, change);
  }

  public void mousePressed(MouseEvent e) {
    dispatchMouseEvent(e, ViewEvents.MOUSE_PRESSED);
  }

  public void mouseReleased(MouseEvent e) {
    if (myDragStart != null) {
      root().validate();
      myDragStart.dispatch(ViewEvents.MOUSE_RELEASED, e);
      myDragStart = null;
    } else {
      dispatchMouseEvent(e, ViewEvents.MOUSE_RELEASED);
    }
  }

  public void mouseMoved(MouseEvent e) {
    changeViewUnderMouse(e, root().viewAt(e.location()));
    dispatchMouseEvent(e, ViewEvents.MOUSE_MOVED);
  }

  public void mouseEntered(MouseEvent e) {
    root().validate();
    changeViewUnderMouse(e, root().viewAt(e.location()));
  }

  public void mouseLeft(MouseEvent e) {
    root().validate();
    changeViewUnderMouse(e, null);
  }

  private void changeViewUnderMouse(final MouseEvent e, final View newView) {
    if (myViewUnderMouse == newView) return;

    if (myViewUnderMouse != null) {
      executeCommand(new Runnable() {
        @Override
        public void run() {
          myViewUnderMouse.dispatch(ViewEvents.MOUSE_LEFT, new MouseEvent(e.location()));
        }
      });
    }

    if (newView != null) {
      executeCommand(new Runnable() {
        @Override
        public void run() {
          newView.dispatch(ViewEvents.MOUSE_ENTERED, new MouseEvent(e.location()));
        }
      });
    }

    myViewUnderMouse = newView;
  }

  public void mouseDragged(final MouseEvent e) {
    executeCommand(new Runnable() {
      @Override
      public void run() {
        root().validate();

        if (myDragStart != null) {
          myDragStart.dispatch(ViewEvents.MOUSE_DRAGGED, e);
        }
      }
    });
  }

  private void dispatchMouseEvent(final MouseEvent e, final ViewEventSpec<MouseEvent> spec) {
    executeCommand(new Runnable() {
      @Override
      public void run() {
        root().validate();

        View target = root().viewAt(e.location());
        if (target == null) {
          target = root();
        }
        if (spec == ViewEvents.MOUSE_PRESSED) {
          myDragStart = target;
        }
        target.dispatch(spec, e);
      }
    });
  }

  public void keyPressed(KeyEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.KEY_PRESSED);
  }

  public void keyReleased(KeyEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.KEY_RELEASED);
  }

  public void keyTyped(KeyEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.KEY_TYPED);
  }

  public void copy(CopyCutEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.COPY);
  }

  public void cut(CopyCutEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.CUT);
  }

  public void paste(PasteEvent e) {
    dispatchFocusBasedEvent(e, ViewEvents.PASTE);
  }

  public void executeCommand(Runnable r) {
    if (myInCommand) {
      r.run();
    } else {
      myInCommand = true;
      myListeners.fire(new ListenerCaller<ViewContainerListener>() {
        @Override
        public void call(ViewContainerListener l) {
          l.onBeforeCommand();
        }
      });
      try {
        r.run();
      } finally {
        myListeners.fire(new ListenerCaller<ViewContainerListener>() {
          @Override
          public void call(ViewContainerListener l) {
            l.onAfterCommand();
          }
        });
        myInCommand = false;
      }
    }
  }

  private <EventT extends Event> void dispatchFocusBasedEvent(final EventT e, final ViewEventSpec<EventT> spec) {
    executeCommand(new Runnable() {
      @Override
      public void run() {
        View target = focusedView().get();
        if (target == null) {
          target = root();
        }
        target.dispatch(spec, e);
      }
    });
  }

  void propertyChanged(final View view, final ViewPropertySpec<?> prop, final PropertyChangeEvent<?> event) {
    myListeners.fire(new ListenerCaller<ViewContainerListener>() {
      @Override
      public void call(ViewContainerListener l) {
        l.onPropertySet(view, prop, event);
      }
    });
  }

  void viewAttached(final View view) {
    myListeners.fire(new ListenerCaller<ViewContainerListener>() {
      @Override
      public void call(ViewContainerListener l) {
        l.onViewAttached(view);
      }
    });
  }

  void viewDetached(final View view) {
    if (myFocusedView.get() == view) {
      myFocusedView.set(null);
    }

    myListeners.fire(new ListenerCaller<ViewContainerListener>() {
      @Override
      public void call(ViewContainerListener l) {
        l.onViewDetached(view);
      }
    });
  }

  public Registration addListener(ViewContainerListener l) {
    return myListeners.add(l);
  }

  public void requestFocus() {
    myPeer.requestFocus();
  }

  public EventDispatchThread getEdt() {
    return myPeer.getEdt();
  }

  private class RootView extends View {
    private GroupView myDecorationView = new GroupView();
    private GroupView myContentRoot = new GroupView();

    private RootView() {
      children().add(myContentRoot);
      children().add(myDecorationView);

      addListener(new ViewAdapter() {
        @Override
        public void onChildAdded(CollectionItemEvent<View> event) {
          throw new IllegalStateException("It's forbidden to add children to root node");
        }

        @Override
        public void onChildRemoved(CollectionItemEvent<View> event) {
          throw new IllegalStateException("It's forbidden to remove children from root node");
        }
      });
    }

    @Override
    public void validate() {
      super.validate();
      for (Runnable r : myOnValidate) {
        r.run();
      }
      myOnValidate.clear();

    }

    @Override
    protected void doValidate(ValidationContext ctx) {
      super.doValidate(ctx);
      Rectangle current = new Rectangle(Vector.ZERO, new Vector(400, 300));
      for (View child : children()) {
        if (!child.visible().get()) continue;
        current = current.union(child.bounds().get());
      }
      ctx.bounds(current, 0);
    }
  }

  private class FocusedViewProperty extends ValueProperty<View> {
    @Override
    public void set(View value) {
      if (value != null) {
        if (!value.focusable().get()) {
          throw new IllegalStateException("cannot set focus: " + value);
        }
        if (value.container() != ViewContainer.this) {
          throw new IllegalArgumentException();
        }
      }

      View oldValue = get();
      if (oldValue == value) return;

      if (oldValue != null) {
        oldValue.focused(false);
      }
      super.set(value);

      if (value != null) {
        value.focused(true);
      }
    }
  }
}