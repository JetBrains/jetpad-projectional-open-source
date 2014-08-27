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
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.projectional.svg.SvgAttrSpec;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgElementListener;
import jetbrains.jetpad.projectional.svg.event.SvgEvents;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.svg.SVGOMElement;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends SVGOMElement> extends SvgNodeMapper<SourceT, TargetT> {
  private Registration myHandlerReg;

  public SvgElementMapper(SourceT source, TargetT target, AbstractDocument doc) {
    super(source, target, doc);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
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

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    final EventListener clickListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_CLICKED, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };
    final EventListener mouseDownListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_PRESSED, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };
    final EventListener mouseUpListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_RELEASED, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };
    final EventListener mouseOverListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_OVER, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };
    final EventListener mouseMoveListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_MOVE, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };
    final EventListener mouseOutListener = new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        getSource().dispatch(SvgEvents.MOUSE_OUT, new MouseEvent(((DOMMouseEvent) evt).getClientX(), ((DOMMouseEvent) evt).getClientY()));
      }
    };

    getTarget().addEventListener("click", clickListener, false);
    getTarget().addEventListener("mousedown", mouseDownListener, false);
    getTarget().addEventListener("mouseup", mouseUpListener, false);
    getTarget().addEventListener("mouseover", mouseOverListener, false);
    getTarget().addEventListener("mousemove", mouseMoveListener, false);
    getTarget().addEventListener("mouseout", mouseOutListener, false);

    myHandlerReg = new Registration() {
      @Override
      public void remove() {
        getTarget().removeEventListener("click", clickListener, false);
        getTarget().removeEventListener("mousedown", mouseDownListener, false);
        getTarget().removeEventListener("mouseup", mouseUpListener, false);
        getTarget().removeEventListener("mouseover", mouseOverListener, false);
        getTarget().removeEventListener("mousemove", mouseMoveListener, false);
        getTarget().removeEventListener("mouseout", mouseOutListener, false);
      }
    };
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    myHandlerReg.remove();
  }
}
