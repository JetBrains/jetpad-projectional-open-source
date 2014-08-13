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
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.model.util.ListMap;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

import java.util.Set;

public class AttrMap implements EventSource<SvgAttributeEvent> {
  private ListMap<String, Property<String>> myListMap = new ListMap<>();
  private Listeners<EventHandler<? super SvgAttributeEvent>> myHandlers;

  public int size() {
    return myListMap.size();
  }

  public boolean isEmpty() {
    return myListMap.isEmpty();
  }

  public boolean containsKey(String key) {
    return myListMap.containsKey(key);
  }

  public Property<String> get(String key) {
    return myListMap.get(key);
  }

  public String put(String key, String value) {
    String oldValue = null;
    if (myListMap.containsKey(key)) {
      oldValue = myListMap.get(key).get();
      myListMap.get(key).set(value);
    } else {
      myListMap.put(key, new ValueProperty<>(value));
    }
    if (value != null && !value.equals(oldValue) && myHandlers != null) {
      final SvgAttributeEvent event = new SvgAttributeEvent(key, oldValue, value);
      myHandlers.fire(new ListenerCaller<EventHandler<? super SvgAttributeEvent>>() {
        @Override
        public void call(EventHandler<? super SvgAttributeEvent> l) {
          l.onEvent(event);
        }
      });
    }
    return oldValue;
  }

  public Property<String> remove(String key) {
    Property<String> result = myListMap.remove(key);
    if (myHandlers != null) {
      final SvgAttributeEvent event = new SvgAttributeEvent(key, result.get(), null);
      myHandlers.fire(new ListenerCaller<EventHandler<? super SvgAttributeEvent>>() {
        @Override
        public void call(EventHandler<? super SvgAttributeEvent> l) {
          l.onEvent(event);
        }
      });
    }
    return result;
  }

  public Set<String> keySet() {
    return myListMap.keySet();
  }

  @Override
  public Registration addHandler(EventHandler<? super SvgAttributeEvent> handler) {
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
