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
package jetbrains.jetpad.projectional.svg.toDom;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgAttrSpec;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgElementListener;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;
import org.vectomatic.dom.svg.OMSVGElement;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends SvgNodeMapper<SourceT, TargetT> {
  private Map<SvgEventSpec, HandlerRegistration> myHandlerRegs;

  public SvgElementMapper(SourceT source, TargetT target) {
    super(source, target);
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

        for (SvgAttrSpec<?> key : getSource().getAttrKeys()) {
          getTarget().setAttribute(key.toString(), getSource().getAttr(key.getName()).get().toString());
        }
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    });

    conf.add(Synchronizers.forPropsOneWay(getSource().getEventPeer().handlersSet(), new WritableProperty<Set<SvgEventSpec>>() {
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
          if (getSource().parent().get() == null) {
            // bug in lib-gwt-svg, getOwnerSvgElement throws exception
            throw new IllegalStateException("Can't add handlers to root svg element");
          }

          switch (spec) {
            case MOUSE_CLICKED:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                  getSource().dispatch(spec, createMouseEvent(clickEvent));
                }
              }, ClickEvent.getType()));
              break;
            case MOUSE_PRESSED:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new MouseDownHandler() {
                @Override
                public void onMouseDown(MouseDownEvent mouseDownEvent) {
                  getSource().dispatch(spec, createMouseEvent(mouseDownEvent));
                }
              }, MouseDownEvent.getType()));
              break;
            case MOUSE_RELEASED:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new MouseUpHandler() {
                @Override
                public void onMouseUp(MouseUpEvent mouseUpEvent) {
                  getSource().dispatch(spec, createMouseEvent(mouseUpEvent));
                }
              }, MouseUpEvent.getType()));
              break;
            case MOUSE_OVER:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent mouseOverEvent) {
                  getSource().dispatch(spec, createMouseEvent(mouseOverEvent));
                }
              }, MouseOverEvent.getType()));
              break;
            case MOUSE_MOVE:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new MouseMoveHandler() {
                @Override
                public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
                  getSource().dispatch(spec, createMouseEvent(mouseMoveEvent));
                }
              }, MouseMoveEvent.getType()));
              break;
            case MOUSE_OUT:
              myHandlerRegs.put(spec, getTarget().addDomHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent mouseOutEvent) {
                  getSource().dispatch(spec, createMouseEvent(mouseOutEvent));
                }
              }, MouseOutEvent.getType()));
              break;
            default:
              break;
          }
        }

        if (myHandlerRegs.isEmpty()) {
          myHandlerRegs = null;
        }
      }
    }));
  }

  private MouseEvent createMouseEvent(com.google.gwt.event.dom.client.MouseEvent<?> evt) {
    evt.stopPropagation();
    return new MouseEvent(evt.getRelativeX(getTarget().getOwnerSVGElement().getElement()),
        evt.getRelativeY(getTarget().getOwnerSVGElement().getElement()));
  }
}
