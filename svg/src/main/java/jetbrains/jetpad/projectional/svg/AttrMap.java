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
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.util.ListMap;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class AttrMap implements EventSource<SvgAttributeEvent<?>> {
  private ListMap<SvgAttrSpec<?>, Object> myAttrs;
  private Listeners<EventHandler<? super SvgAttributeEvent<?>>> myHandlers;

  public int size() {
    return (myAttrs == null ? 0 : myAttrs.size());
  }

  public boolean isEmpty() {
    return (myAttrs == null || myAttrs.isEmpty());
  }

  public boolean containsKey(SvgAttrSpec<?> key) {
    return (myAttrs != null && myAttrs.containsKey(key));
  }

  public <ValueT> ValueT get(SvgAttrSpec<ValueT> spec) {
    if (myAttrs != null && myAttrs.containsKey(spec)) {
      return (ValueT) myAttrs.get(spec);
    }
    return null;
  }

  public <ValueT> ValueT set(SvgAttrSpec<ValueT> spec, ValueT value) {
    if (myAttrs == null) {
      myAttrs = new ListMap<>();
    }

    ValueT oldValue;
    if (value == null) {
      oldValue = (ValueT) myAttrs.remove(spec);
    } else {
      oldValue = (ValueT) myAttrs.put(spec, value);
    }

    if (value != null && !value.equals(oldValue) && myHandlers != null) {
      final SvgAttributeEvent<ValueT> event = new SvgAttributeEvent<>(spec, oldValue, value);
      myHandlers.fire(new ListenerCaller<EventHandler<? super SvgAttributeEvent<?>>>() {
        @Override
        public void call(EventHandler<? super SvgAttributeEvent<?>> l) {
          l.onEvent(event);
        }
      });
    }

    return oldValue;
  }

  public <ValueT> ValueT remove(SvgAttrSpec<ValueT> spec) {
    return set(spec, null);
  }

  public Set<SvgAttrSpec<?>> keySet() {
    if (myAttrs == null) {
      return Collections.emptySet();
    }
    return myAttrs.keySet();
  }

  @Override
  public Registration addHandler(EventHandler<? super SvgAttributeEvent<?>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<>();
    }

    final Registration reg = myHandlers.add(handler);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myHandlers.isEmpty()) {
          myHandlers = null;
        }
      }
    };
  }
}
