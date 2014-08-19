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
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class SvgElement extends SvgNode {
  private AttrMap myAttrs = new AttrMap();
  private Listeners<SvgElementListener<?>> myListeners;
  protected static Map<String, SvgAttrSpec<?>> myAttrInfo = new HashMap<>();

  protected SvgElement() {
    super();

    myAttrs.addHandler(new EventHandler<SvgAttributeEvent<?>>() {
      @Override
      public void onEvent(final SvgAttributeEvent<?> event) {
        if (myListeners != null) {
          myListeners.fire(new ListenerCaller<SvgElementListener<?>>() {
            @Override
            public void call(SvgElementListener l) {
              l.onAttrSet(event);
            }
          });
        }
        container().attributeChanged(SvgElement.this, event);
      }
    });
  }

  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return myAttrInfo;
  }

  public SvgAttrSpec<?> getSpecByName(String name) {
    if (myAttrInfo.containsKey(name)) {
      return myAttrInfo.get(name);
    }
    return SvgXmlAttrSpec.createSpec(name);
  }

  public <ValueT> Property<ValueT> getAttr(final SvgAttrSpec<ValueT> spec) {
    return new Property<ValueT>() {
      @Override
      public String getPropExpr() {
        return this + "." + spec;
      }

      @Override
      public ValueT get() {
        return myAttrs.get(spec);
      }

      @Override
      public void set(ValueT value) {
        myAttrs.set(spec, value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
        return addListener(new SvgElementListener<ValueT>() {
          @Override
          public void onAttrSet(SvgAttributeEvent<ValueT> event) {
            if (spec != event.getAttrSpec()) {
              return;
            }
            handler.onEvent(new PropertyChangeEvent<>(event.getOldValue(), event.getNewValue()));
          }
        });
      }
    };
  }

  public <ValueT> Property<ValueT> getAttr(String name) {
    return getAttr((SvgAttrSpec<ValueT>) getSpecByName(name));
  }

  public Set<SvgAttrSpec<?>> getAttrKeys() {
    return myAttrs.keySet();
  }

  public <ValueT> Registration addListener(SvgElementListener<ValueT> l) {
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
}
