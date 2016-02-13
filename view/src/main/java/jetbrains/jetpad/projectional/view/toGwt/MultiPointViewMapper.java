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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.MultiPointView;
import jetbrains.jetpad.projectional.view.PolyLineView;
import jetbrains.jetpad.values.Color;

class MultiPointViewMapper extends BaseViewMapper<MultiPointView, Element> {
  MultiPointViewMapper(ViewToDomContext ctx, MultiPointView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    final Element svg = createSVG();
    getTarget().appendChild(svg);

    final Element polyLine = SvgUtil.createPolyline();
    svg.appendChild(polyLine);

    final Style style = polyLine.getStyle();

    final boolean isPolyLine = getSource() instanceof PolyLineView;

    style.setProperty("strokeWidth", "1");

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
        StringBuilder points = new StringBuilder();

        Vector origin = getSource().bounds().get().origin;
        for (Vector v : getSource().points) {
          v = v.sub(origin);

          //we need this shifting to fix disappearing lines in FF
          double x = v.x != 0 ? v.x : 1;
          double y = v.y != 0 ? v.y : 1;

          if (points.length() > 0) {
            points.append(" ");
          }
          points.append(x).append(",").append(y);
        }

        polyLine.setAttribute("points", points.toString());
      }
    });

    conf.add(GwtViewSynchronizers.boundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forPropsOneWay(getSource().color(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        if (isPolyLine) {
          style.setProperty("stroke", value.toCssColor());
          style.setProperty("fill", "none");
        } else {
          style.setProperty("stroke", "none");
          style.setProperty("fill", value.toCssColor());
        }
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().width(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        style.setProperty("strokeWidth", "" + value);
      }
    }));
  }
}