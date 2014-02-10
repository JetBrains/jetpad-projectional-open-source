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

import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.util.ListMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ViewTrait {
  private ListMap<ViewPropertySpec<?>, Object> myProperties;
  private ListMap<ViewEventSpec<?>, List<ViewEventHandler>> myHandlers;

  ViewTrait(Map<ViewPropertySpec<?>, Object> props, Map<ViewEventSpec<?>, List<ViewEventHandler>> handlers) {
    for (Map.Entry<ViewPropertySpec<?>, Object> entry : props.entrySet()) {
      if (myProperties == null) {
        myProperties = new ListMap<>();
      }
      myProperties.put(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<ViewEventSpec<?>, List<ViewEventHandler>> entry : handlers.entrySet()) {
      if (myHandlers == null) {
        myHandlers = new ListMap<>();
      }
      myHandlers.put(entry.getKey(), entry.getValue());
    }
  }

  Set<ViewPropertySpec<?>> properties() {
    if (myProperties == null) return Collections.emptySet();
    return Collections.unmodifiableSet(myProperties.keySet());
  }

  boolean hasValue(ViewPropertySpec<?> prop) {
    if (myProperties == null) return false;
    return myProperties.containsKey(prop);
  }

  public <ValueT> ValueT get(ViewPropertySpec<ValueT> prop) {
    return (ValueT) myProperties.get(prop);
  }

  <EventT extends Event> void dispatch(View view, ViewEventSpec<EventT> spec, EventT event) {
    if (myHandlers != null && myHandlers.containsKey(spec)) {
      for (ViewEventHandler handler : myHandlers.get(spec)) {
        handler.handle(view, event);
        if (event.isConsumed()) return;
      }
    }
  }
}