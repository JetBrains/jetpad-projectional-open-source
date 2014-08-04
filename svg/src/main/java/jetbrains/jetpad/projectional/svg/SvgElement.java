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
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.util.ListMap;

public class SvgElement extends HasParent<SvgElement, SvgElement> {
  private SvgContainer myContainer;
  private ListMap<SvgPropertySpec<?>, Object> myProperties;
  private Listeners<SvgElementListener> myListeners;

  private SvgChildList myChildren;

  public SvgContainer container() {
    return myContainer;
  }

  public ObservableList<SvgElement> children() {
    if (myChildren == null) {
      myChildren = new SvgChildList(this);
    }
    return myChildren;
  }

  <ValueT> ValueT get(SvgPropertySpec<ValueT> spec) {
    if (myProperties != null && myProperties.containsKey(spec)) {
      return (ValueT) myProperties.get(spec);
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
    fire(new ListenerCaller<SvgElementListener>() {
      @Override
      public void call(SvgElementListener l) {
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
        return SvgElement.this.get(spec);
      }

      @Override
      public void set(ValueT value) {
        SvgElement.this.set(spec, value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
        return addListener(new SvgElementAdapter() {
          @Override
          public <ValT> void onPropertySet(SvgPropertySpec<ValT> p, PropertyChangeEvent<ValT> event) {
            if (p != spec) return;
            handler.onEvent((PropertyChangeEvent<ValueT>) event);
          }
        });
      }
    };
  }

  Registration addListener(SvgElementListener l) {
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

  protected void fire(ListenerCaller<SvgElementListener> caller) {
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

    for (SvgElement element : children()) {
      element.attach(container);
    }

    myContainer = container;
    myContainer.svgElementAttached(this);

    onAttach();

    fire(new ListenerCaller<SvgElementListener>() {
      @Override
      public void call(SvgElementListener l) {
        l.onSvgElementAttached();
      }
    });
  }

  void detach() {
    if (!isAttached()) {
      throw new IllegalStateException("Svg element is not attached");
    }

    fire(new ListenerCaller<SvgElementListener>() {
      @Override
      public void call(SvgElementListener l) {
        l.onSvgElementDetached();
      }
    });

    onDetach();

    for (SvgElement element : children()) {
      element.detach();
    }

    myContainer.svgElementDetached(this);
    myContainer = null;
  }

  private class SvgChildList extends ChildList<SvgElement, SvgElement> {
    public SvgChildList(SvgElement parent) {
      super(parent);
    }

    @Override
    public void add(int index, SvgElement element) {
      if (isAttached()) {
        element.attach(container());
      }
      super.add(index, element);
    }

    @Override
    public SvgElement remove(int index) {
      SvgElement element = get(index);
      if (isAttached()) {
        element.detach();
      }
      return super.remove(index);
    }
  }
}
