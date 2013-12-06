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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;

import java.util.ArrayList;
import java.util.List;

public class ViewContainer {
  private ViewContainerPeer myPeer = new NullViewContainerPeer();
  private View myRoot = new RootView();
  private Property<View> myFocusedView = new FocusedViewProperty();
  private View myDragStart;
  private List<Runnable> myOnValidate = new ArrayList<Runnable>();
  private Listeners<ViewContainerListener> myListeners = new Listeners<ViewContainerListener>();

  public ViewContainer() {
    myPeer.attach(this);
    myRoot.attach(this);
  }

  public Property<View> focusedView() {
    return myFocusedView;
  }

  public View root() {
    return myRoot;
  }

  public void whenValid(Runnable r) {
    if (myRoot.valid().get()) {
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
    myRoot.invalidateTree();
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
    dispatchMouseEvent(e, ViewEvents.MOUSE_MOVED);
  }

  public void mouseDragged(MouseEvent e) {
    root().validate();

    if (myDragStart != null) {
      myDragStart.dispatch(ViewEvents.MOUSE_DRAGGED, e);
    }
  }

  private void dispatchMouseEvent(MouseEvent e, ViewEventSpec<MouseEvent> spec) {
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

  private <EventT extends Event> void dispatchFocusBasedEvent(EventT e, ViewEventSpec<EventT> spec) {
    View target = focusedView().get();
    if (target == null) {
      target = root();
    }
    target.dispatch(spec, e);
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

  private class RootView extends View {
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
        if (!value.focusable().get()) throw new IllegalStateException("cannot set focus: " + value);
        if (value.container() != ViewContainer.this) throw new IllegalArgumentException();
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