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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.projectional.svg.SvgAttrSpec;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgElementListener;
import jetbrains.jetpad.projectional.svg.SvgEvents;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.vectomatic.dom.svg.OMSVGElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends SvgNodeMapper<SourceT, TargetT> {
  private Registration myHandlerReg;

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
  }

  private MouseEvent createMouseEvent(com.google.gwt.event.dom.client.MouseEvent<?> evt) {
    return new MouseEvent(evt.getRelativeX(getTarget().getOwnerSVGElement().getElement()),
        evt.getRelativeY(getTarget().getOwnerSVGElement().getElement()));
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    final HandlerRegistration clickReg = getTarget().addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        getSource().dispatch(SvgEvents.MOUSE_CLICKED, createMouseEvent(clickEvent));
      }
    }, ClickEvent.getType());

    final HandlerRegistration downReg = getTarget().addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent mouseDownEvent) {
        getSource().dispatch(SvgEvents.MOUSE_PRESSED, createMouseEvent(mouseDownEvent));
      }
    }, MouseDownEvent.getType());

    final HandlerRegistration upReg = getTarget().addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent mouseUpEvent) {
        getSource().dispatch(SvgEvents.MOUSE_RELEASED, createMouseEvent(mouseUpEvent));
      }
    }, MouseUpEvent.getType());

    final HandlerRegistration overReg = getTarget().addDomHandler(new MouseOverHandler() {
      @Override
      public void onMouseOver(MouseOverEvent mouseOverEvent) {
        getSource().dispatch(SvgEvents.MOUSE_OVER, createMouseEvent(mouseOverEvent));
      }
    }, MouseOverEvent.getType());

    final HandlerRegistration moveReg = getTarget().addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
        getSource().dispatch(SvgEvents.MOUSE_MOVE, createMouseEvent(mouseMoveEvent));
      }
    }, MouseMoveEvent.getType());

    final HandlerRegistration outReg = getTarget().addDomHandler(new MouseOutHandler() {
      @Override
      public void onMouseOut(MouseOutEvent mouseOutEvent) {
        getSource().dispatch(SvgEvents.MOUSE_OUT, createMouseEvent(mouseOutEvent));
      }
    }, MouseOutEvent.getType());



    myHandlerReg = new Registration() {
      @Override
      public void remove() {
        clickReg.removeHandler();
        downReg.removeHandler();
        upReg.removeHandler();
        overReg.removeHandler();
        moveReg.removeHandler();
        outReg.removeHandler();
      }
    };
  }

  @Override
  protected void onDetach() {
    super.onDetach();
  }
}
