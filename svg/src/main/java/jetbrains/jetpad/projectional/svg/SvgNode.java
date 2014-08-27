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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.projectional.svg.event.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

import java.util.HashMap;
import java.util.Map;

public abstract class SvgNode extends HasParent<SvgNode, SvgNode> {
  private SvgContainer myContainer;
  private Listeners<SvgNodeListener> myListeners;
  private Map<SvgEventSpec, Listeners<SvgEventHandler<?>>> myEventHandlers;


  private SvgChildList myChildren;

  public SvgContainer container() {
    return myContainer;
  }

  public ObservableList<SvgNode> children() {
    if (myChildren == null) {
      myChildren = new SvgChildList(this);
    }
    return myChildren;
  }

  public <EventT extends Event> Registration addEventHandler(SvgEventSpec spec, SvgEventHandler<EventT> handler) {
    if (myEventHandlers == null) {
      myEventHandlers = new HashMap<>();
    }
    if (!myEventHandlers.containsKey(spec)) {
      myEventHandlers.put(spec, new Listeners<SvgEventHandler<?>>());
    }

    // TODO: let mappers know about new event handler (to make them add target's listeners)

    return (myEventHandlers.get(spec).add(handler));
  }

  Registration addListener(SvgNodeListener l) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    final Registration reg = myListeners.add(l);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myListeners.isEmpty()) {
          myListeners = null;
        }
      }
    };
  }

  protected void fire(ListenerCaller<SvgNodeListener> caller) {
    if (myListeners != null) {
      myListeners.fire(caller);
    }
  }


  public boolean isAttached() {
    return myContainer != null;
  }

  protected void onAttach() {
  }

  protected void onDetach() {
  }

  void attach(SvgContainer container) {
    if (isAttached()) {
      throw new IllegalStateException("Svg element is already attached");
    }

    for (SvgNode node : children()) {
      node.attach(container);
    }

    myContainer = container;
    myContainer.svgNodeAttached(this);

    onAttach();

    fire(new ListenerCaller<SvgNodeListener>() {
      @Override
      public void call(SvgNodeListener l) {
        l.onSvgNodeAttached();
      }
    });
  }

  void detach() {
    if (!isAttached()) {
      throw new IllegalStateException("Svg element is not attached");
    }

    fire(new ListenerCaller<SvgNodeListener>() {
      @Override
      public void call(SvgNodeListener l) {
        l.onSvgNodeDetached();
      }
    });

    onDetach();

    for (SvgNode node : children()) {
      node.detach();
    }

    myContainer.svgNodeDetached(this);
    myContainer = null;
  }

  public <EventT extends Event> void dispatch(SvgEventSpec spec, final EventT event) {
    if (myEventHandlers != null && myEventHandlers.containsKey(spec)) {
      myEventHandlers.get(spec).fire(new ListenerCaller<SvgEventHandler<?>>() {
        @Override
        public void call(SvgEventHandler<?> l) {
          if (event.isConsumed()) return;
          ((SvgEventHandler<EventT>) l).handle(SvgNode.this, event);
        }
      });
    }

    if (parent().get() != null && !event.isConsumed()) {
      parent().get().dispatch(spec, event);
    }
  }

  private class SvgChildList extends ChildList<SvgNode, SvgNode> {
    public SvgChildList(SvgNode parent) {
      super(parent);
    }

    @Override
    public void add(final int index, final SvgNode node) {
      if (isAttached()) {
        node.attach(container());
      }
      super.add(index, node);
      fire(new ListenerCaller<SvgNodeListener>() {
        @Override
        public void call(SvgNodeListener l) {
          l.onChildAdded(new CollectionItemEvent<>(node, index, true));
        }
      });
    }

    @Override
    public SvgNode remove(final int index) {
      final SvgNode node = get(index);
      if (isAttached()) {
        node.detach();
      }
      SvgNode result =  super.remove(index);
      fire(new ListenerCaller<SvgNodeListener>() {
        @Override
        public void call(SvgNodeListener l) {
          l.onChildRemoved(new CollectionItemEvent<>(node, index, false));
        }
      });
      return result;
    }
  }
}
