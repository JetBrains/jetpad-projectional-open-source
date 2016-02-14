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
package jetbrains.jetpad.projectional.svg.toDom;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import elemental.dom.Node;
import elemental.svg.SVGElement;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgAttributeSpec;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgElementListener;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

class SvgElementMapper<SourceT extends SvgElement, TargetT extends SVGElement> extends SvgNodeMapper<SourceT, TargetT> {
  private Map<SvgEventSpec, HandlerRegistration> myHandlerRegs;
  private SvgGwtPeer myPeer;

  public SvgElementMapper(SourceT source, TargetT target, SvgGwtPeer peer) {
    super(source, target, peer);
    myPeer = peer;
  }

  @Override
  protected void registerSynchronizers(final SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(new Synchronizer() {
      private Registration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = getSource().addListener(new SvgElementListener<Object>() {
          @Override
          public void onAttrSet(SvgAttributeEvent<Object> event) {
            if (event.getNewValue() == null) {
              getTarget().removeAttribute(event.getAttrSpec().toString());
            }
            getTarget().setAttribute(event.getAttrSpec().toString(), event.getNewValue().toString());
          }
        });

        for (SvgAttributeSpec<?> key : getSource().getAttributeKeys()) {
          getTarget().setAttribute(key.toString(), getSource().getAttribute(key.getName()).get().toString());
        }
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    });

    conf.add(Synchronizers.forPropsOneWay(getSource().handlersSet(), new WritableProperty<Set<SvgEventSpec>>() {
      @Override
      public void set(Set<SvgEventSpec> value) {
        if (myHandlerRegs == null) {
          myHandlerRegs = new EnumMap<>(SvgEventSpec.class);
        }

        for (final SvgEventSpec spec : SvgEventSpec.values()) {
          if (!value.contains(spec) && myHandlerRegs.containsKey(spec)) {
            myHandlerRegs.remove(spec).removeHandler();
          }
          if (!value.contains(spec) || myHandlerRegs.containsKey(spec)) continue;


          String event = null;
          switch (spec) {
            case MOUSE_CLICKED:
              event = "click";
              break;
            case MOUSE_PRESSED:
              event = "mousedown";
              break;
            case MOUSE_RELEASED:
              event = "mouseup";
              break;
            case MOUSE_OVER:
              event = "mouseover";
              break;
            case MOUSE_MOVE:
              event = "mousemove";
              break;
            case MOUSE_OUT:
              event = "mouseout";
              break;
            default:
              break;
          }

          if (event == null) {
            throw new IllegalStateException();
          }

          final GQuery tq = GQuery.$(getTarget()).on(event, new Function() {
            @Override
            public boolean f(Event e) {
              MouseEvent mouseEvent = createMouseEvent(e);
              getSource().dispatch(spec, mouseEvent);
              return true;
            }
          });

          final String eventToRemove = event;
          myHandlerRegs.put(spec, new HandlerRegistration() {
            @Override
            public void removeHandler() {
              tq.off(eventToRemove);
            }
          });
        }

        if (myHandlerRegs.isEmpty()) {
          myHandlerRegs = null;
        }
      }
    }));
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    if (myHandlerRegs != null) {
      for (HandlerRegistration registration : myHandlerRegs.values()) {
        registration.removeHandler();
      }
      myHandlerRegs.clear();
    }
  }

  private MouseEvent createMouseEvent(com.google.gwt.user.client.Event evt) {
    evt.stopPropagation();
    DoubleVector coords = myPeer.inverseScreenTransform(getSource(), new DoubleVector(evt.getClientX(), evt.getClientY()));
    return new MouseEvent((int) coords.x, (int) coords.y);
  }
}

