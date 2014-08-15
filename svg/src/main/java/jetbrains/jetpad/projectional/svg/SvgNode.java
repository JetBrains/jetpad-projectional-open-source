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

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.util.ListMap;

import java.util.*;

public class SvgNode extends HasParent<SvgNode, SvgNode> {
  private SvgContainer myContainer;
  private ListMap<SvgPropertySpec<?>, Object> myProperties;
  private List<SvgTrait> myTraits;
  private Listeners<SvgNodeListener> myListeners;

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

  <ValueT> ValueT get(SvgPropertySpec<ValueT> spec) {
    if (myProperties != null && myProperties.containsKey(spec)) {
      @SuppressWarnings("unchecked")
      ValueT result = (ValueT) myProperties.get(spec);
      return result;
    }

    if (myTraits != null) {
      for (SvgTrait t : myTraits) {
        if (t.hasValue(spec)) {
          return t.get(spec);
        }
      }
    }

    return spec.defaultValue();
  }

  <ValueT> void set(SvgPropertySpec<ValueT> spec, ValueT value) {
    final ValueT oldValue = get(spec);

    if (myProperties == null && value == null) return;
    if (myProperties == null) {
      myProperties = new ListMap<>();
    }

    if (value == null) {
      myProperties.remove(spec);
      if (myProperties.isEmpty()) {
        myProperties = null;
      }
    } else {
      myProperties.put(spec, value);
    }

    final ValueT newValue = get(spec);
    if (!Objects.equal(oldValue, newValue)) {
      propertyChanged(spec, new PropertyChangeEvent<>(oldValue, newValue));
    }
  }

  private <ValueT> void propertyChanged(final SvgPropertySpec<ValueT> spec, final PropertyChangeEvent<ValueT> event) {
    if (isAttached()) {
      container().propertyChanged(this, spec, event);
    }
    fire(new ListenerCaller<SvgNodeListener>() {
      @Override
      public void call(SvgNodeListener l) {
        l.onPropertySet(spec, event);
      }
    });
  }

  public <ValueT> Property<ValueT> getProp(final SvgPropertySpec<ValueT> spec) {
    return new Property<ValueT>() {
      @Override
      public String getPropExpr() {
        return this + "." + spec;
      }

      @Override
      public ValueT get() {
        return SvgNode.this.get(spec);
      }

      @Override
      public void set(ValueT value) {
        SvgNode.this.set(spec, value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
        return addListener(new SvgNodeAdapter() {
          @Override
          public <ValT> void onPropertySet(SvgPropertySpec<ValT> p, PropertyChangeEvent<ValT> event) {
            if (p != spec) return;
            @SuppressWarnings("unchecked")
            PropertyChangeEvent<ValueT> castEvent = (PropertyChangeEvent<ValueT>) event;
            handler.onEvent(castEvent);
          }
        });
      }
    };
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
    myContainer.svgElementAttached(this);

    onAttach();

    fire(new ListenerCaller<SvgNodeListener>() {
      @Override
      public void call(SvgNodeListener l) {
        l.onSvgElementAttached();
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
        l.onSvgElementDetached();
      }
    });

    onDetach();

    for (SvgNode node : children()) {
      node.detach();
    }

    myContainer.svgElementDetached(this);
    myContainer = null;
  }

  public Registration addTrait(final SvgTrait trait) {
    if (myTraits == null) {
      myTraits = new ArrayList<>();
    }

    Runnable fire = createFiringRunnable(trait);
    myTraits.add(trait);
    fire.run();

    return new Registration() {
      @Override
      public void remove() {
        Runnable fire = createFiringRunnable(trait);
        myTraits.remove(trait);
        fire.run();

        if (myTraits.isEmpty()) {
          myTraits = null;
        }
      }
    };
  }

  private <ValueT> Runnable createPropChangedRunnable(final SvgPropertySpec<ValueT> spec) {
    final ValueT val = get(spec);
    return new Runnable() {
      @Override
      public void run() {
        ValueT newVal = get(spec);
        if (Objects.equal(val, newVal)) return;
        propertyChanged(spec, new PropertyChangeEvent<>(val, newVal));
      }
    };
  }

  private Runnable createFiringRunnable(SvgTrait t) {
    Set<SvgPropertySpec<?>> props = new HashSet<>();
    final List<Runnable> toRun = new ArrayList<>();

    for (final SvgPropertySpec<?> p : t.properties()) {
      if (props.contains(p)) continue;
      toRun.add(createPropChangedRunnable(p));
      props.add(p);
    }

    return new Runnable() {
      @Override
      public void run() {
        for (Runnable r : toRun) {
          r.run();
        }
      }
    };
  }

  public <EventT extends Event> void dispatch(SvgEventSpec<EventT> spec, EventT event) {
    if (myTraits != null) {
      for (SvgTrait t : myTraits) {
        t.dispatch(this, spec, event);
        if (event.isConsumed()) return;
      }
    }

    if (parent().get() != null) {
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
