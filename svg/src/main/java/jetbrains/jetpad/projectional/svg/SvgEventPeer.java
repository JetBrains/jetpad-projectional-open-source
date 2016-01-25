/*
 * Copyright 2012-2016 JetBrains s.r.o
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
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.projectional.svg.event.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

class SvgEventPeer {
  private Map<SvgEventSpec, Listeners<SvgEventHandler<?>>> myEventHandlers;
  private Listeners<EventHandler<? super PropertyChangeEvent<Set<SvgEventSpec>>>> myListeners;

  ReadableProperty<Set<SvgEventSpec>> handlersSet() {
    return new ReadableProperty<Set<SvgEventSpec>>() {
      @Override
      public String getPropExpr() {
        return this + "." + "handlersProp";
      }

      @Override
      public Set<SvgEventSpec> get() {
        return handlersKeySet();
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Set<SvgEventSpec>>> handler) {
        if (myListeners == null) {
          myListeners = new Listeners<>();
        }
        final Registration addReg = myListeners.add(handler);
        return new Registration() {
          @Override
          protected void doRemove() {
            addReg.remove();
            if (myListeners.isEmpty()) {
              myListeners = null;
            }
          }
        };
      }
    };
  }

  private Set<SvgEventSpec> handlersKeySet() {
    return myEventHandlers == null ? EnumSet.noneOf(SvgEventSpec.class) : myEventHandlers.keySet();
  }

  <EventT extends Event> Registration addEventHandler(final SvgEventSpec spec, SvgEventHandler<EventT> handler) {
    if (myEventHandlers == null) {
      myEventHandlers = new EnumMap<>(SvgEventSpec.class);
    }
    if (!myEventHandlers.containsKey(spec)) {
      myEventHandlers.put(spec, new Listeners<SvgEventHandler<?>>());
    }

    final Set<SvgEventSpec> oldHandlersSet = myEventHandlers.keySet();

    final Registration addReg = myEventHandlers.get(spec).add(handler);
    Registration disposeReg = new Registration() {
      @Override
      protected void doRemove() {
        addReg.remove();
        if (myEventHandlers.get(spec).isEmpty()) {
          myEventHandlers.remove(spec);
        }
      }
    };

    if (myListeners != null) {
      myListeners.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<Set<SvgEventSpec>>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<Set<SvgEventSpec>>> l) {
          l.onEvent(new PropertyChangeEvent<>(oldHandlersSet, handlersKeySet()));
        }
      });
    }

    return disposeReg;
  }

  <EventT extends Event> void dispatch(SvgEventSpec spec, final EventT event, final SvgNode target) {
    if (myEventHandlers != null && myEventHandlers.containsKey(spec)) {
      myEventHandlers.get(spec).fire(new ListenerCaller<SvgEventHandler<?>>() {
        @Override
        public void call(SvgEventHandler<?> l) {
          if (event.isConsumed()) return;
          ((SvgEventHandler<EventT>) l).handle(target, event);
        }
      });
    }
  }
}