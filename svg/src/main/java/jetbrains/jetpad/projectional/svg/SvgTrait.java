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

import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.util.ListMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SvgTrait {
  private ListMap<SvgPropertySpec<?>, Object> myProperties;
  private ListMap<SvgEventSpec<? extends Event>, List<SvgEventHandler<? extends Event>>> myHandlers;


  SvgTrait(Map<SvgPropertySpec<?>, Object> props, Map<SvgEventSpec<? extends Event>, List<SvgEventHandler<? extends Event>>> handlers) {
    for (Map.Entry<SvgPropertySpec<?>, Object> entry : props.entrySet()) {
      if (myProperties == null) {
        myProperties = new ListMap<>();
      }
      myProperties.put(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<SvgEventSpec<? extends Event>, List<SvgEventHandler<? extends Event>>> entry : handlers.entrySet()) {
      if (myHandlers == null) {
        myHandlers = new ListMap<>();
      }
      myHandlers.put(entry.getKey(), entry.getValue());
    }
  }

  Set<SvgPropertySpec<?>> properties() {
    if (myProperties == null) return Collections.emptySet();
    return Collections.unmodifiableSet(myProperties.keySet());
  }

  boolean hasValue(SvgPropertySpec<?> spec) {
    return (myProperties != null) && myProperties.containsKey(spec);
  }

  public <ValueT> ValueT get(SvgPropertySpec<ValueT> spec) {
    @SuppressWarnings("unchecked")
    ValueT result = (ValueT) myProperties.get(spec);
    return result;
  }

  <EventT extends Event> void dispatch(SvgElement element, SvgEventSpec<EventT> spec, EventT event) {
    if (myHandlers != null && myHandlers.containsKey(spec)) {
      for (SvgEventHandler<? extends Event> handler : myHandlers.get(spec)) {
        @SuppressWarnings("unchecked")
        SvgEventHandler<EventT> castHandler = (SvgEventHandler<EventT>) handler;
        castHandler.handle(element, event);
        if (event.isConsumed()) return;
      }
    }
  }
}
