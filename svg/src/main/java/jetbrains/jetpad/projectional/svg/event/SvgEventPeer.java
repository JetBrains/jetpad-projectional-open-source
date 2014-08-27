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
package jetbrains.jetpad.projectional.svg.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.projectional.svg.SvgNode;

import java.util.EnumMap;
import java.util.Map;

public class SvgEventPeer {
  private Map<SvgEventSpec, Listeners<SvgEventHandler<?>>> myEventHandlers = new EnumMap<>(SvgEventSpec.class);
  private Map<SvgEventSpec, Listeners<EventHandler<? super PropertyChangeEvent<Boolean>>>> myListeners = new EnumMap<>(SvgEventSpec.class);

  public ReadableProperty<Boolean> hasHandlers(final SvgEventSpec spec) {
    return new ReadableProperty<Boolean>() {
      @Override
      public String getPropExpr() {
        return this + "." + spec;
      }

      @Override
      public Boolean get() {
        return (myEventHandlers.containsKey(spec) && !myEventHandlers.get(spec).isEmpty());
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        if (!myListeners.containsKey(spec)) {
          myListeners.put(spec, new Listeners<EventHandler<? super PropertyChangeEvent<Boolean>>>());
        }
        return new Registration() {
          Registration myReg = myListeners.get(spec).add(handler);
          public void remove() {
            myReg.remove();
            if (myListeners.get(spec).isEmpty()) {
              myListeners.remove(spec);
            }
          }
        };
      }
    };
  }

  public <EventT extends Event> Registration addEventHandler(SvgEventSpec spec, SvgEventHandler<EventT> handler) {
    if (!myEventHandlers.containsKey(spec)) {
      myEventHandlers.put(spec, new Listeners<SvgEventHandler<?>>());
    }

    Registration addReg = myEventHandlers.get(spec).add(handler);

    if (myListeners.containsKey(spec)) {
      final Boolean oldHasHandlers = !myEventHandlers.get(spec).isEmpty();
      myListeners.get(spec).fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<Boolean>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<Boolean>> l) {
          l.onEvent(new PropertyChangeEvent<>(oldHasHandlers, true));
        }
      });
    }

    return addReg;
  }

  public <EventT extends Event> void dispatch(SvgEventSpec spec, final EventT event, final SvgNode target) {
    if (myEventHandlers.containsKey(spec)) {
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
