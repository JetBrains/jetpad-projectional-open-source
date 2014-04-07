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
package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.MultiPointView;
import jetbrains.jetpad.projectional.view.PolyLineView;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.*;
import org.vectomatic.dom.svg.utils.OMSVGParser;
import org.vectomatic.dom.svg.utils.SVGConstants;

class MultiPointViewMapper extends BaseViewMapper<MultiPointView, Element> {
  MultiPointViewMapper(View2DomContext ctx, MultiPointView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    OMSVGDocument doc = OMSVGParser.createDocument();
    final OMSVGSVGElement svg = createSVG(doc);
    getTarget().appendChild(svg.getElement());

    final OMSVGPolylineElement polyLine = new OMSVGPolylineElement();
    svg.appendChild(polyLine);

    final OMSVGStyle style = polyLine.getStyle();

    final boolean isPolyLine = getSource() instanceof PolyLineView;

    style.setSVGProperty(SVGConstants.SVG_STROKE_WIDTH_VALUE, "1");

    conf.add(new Synchronizer() {
      private Registration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = getSource().customFeatureChange(MultiPointView.POINTS).addHandler(new EventHandler<Object>() {
          @Override
          public void onEvent(Object event) {
            whenValid(new Runnable() {
              @Override
              public void run() {
                updatePoints();
              }
            });
          }
        });
        whenValid(new Runnable() {
          @Override
          public void run() {
            updatePoints();
          }
        });
      }

      @Override
      public void detach() {
        myReg.remove();
      }

      private void updatePoints() {
        OMSVGPointList points = polyLine.getPoints();
        points.clear();

        Vector origin = getSource().bounds().get().origin;
        for (Vector v : getSource().points) {
          v = v.sub(origin);
          //we need this shifting to fix disappearing lines in FF
          points.appendItem(svg.createSVGPoint(v.x != 0 ? v.x : 1, v.y != 0 ? v.y : 1));
        }
      }
    });

    conf.add(GwtViewSynchronizers.svgBoundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forProperty(getSource().color(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        if (isPolyLine) {
          style.setSVGProperty(SVGConstants.SVG_STROKE_ATTRIBUTE, value.toCssColor());
          style.setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, "none");
        } else {
          style.setSVGProperty(SVGConstants.SVG_STROKE_ATTRIBUTE, "none");
          style.setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, value.toCssColor());
        }
      }
    }));

    conf.add(Synchronizers.forProperty(getSource().width(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        style.setSVGProperty(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "" + value);
      }
    }));
  }
}