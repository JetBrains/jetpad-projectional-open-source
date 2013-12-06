/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.LineView;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;
import org.vectomatic.dom.svg.utils.SVGConstants;

class LineViewMapper extends BaseViewMapper<LineView, Element> {
  LineViewMapper(View2DomContext ctx, LineView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    OMSVGDocument doc = OMSVGParser.createDocument();
    final OMSVGSVGElement svg = doc.createSVGSVGElement();
    getTarget().appendChild(svg.getElement());

    final OMSVGLineElement line = new OMSVGLineElement();
    svg.appendChild(line);

    conf.add(GwtViewSynchronizers.svgBoundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forProperty(new DerivedProperty<Vector>(getSource().bounds(), getSource().start()) {
      @Override
      public Vector get() {
        return getSource().start().get().sub(getSource().bounds().get().origin);
      }
    }, new WritableProperty<Vector>() {
      @Override
      public void set(final Vector value) {
        whenValid(new Runnable() {
          @Override
          public void run() {
            line.getX1().getBaseVal().setValue(value.x + 1);
            line.getY1().getBaseVal().setValue(value.y + 1);
          }
        });
      }
    }));
    conf.add(Synchronizers.forProperty(new DerivedProperty<Vector>(getSource().bounds(), getSource().start()) {
      @Override
      public Vector get() {
        return getSource().end().get().sub(getSource().bounds().get().origin);
      }
    }, new WritableProperty<Vector>() {
      @Override
      public void set(final Vector value) {
        whenValid(new Runnable() {
          @Override
          public void run() {
            line.getX2().getBaseVal().setValue(value.x + 1);
            line.getY2().getBaseVal().setValue(value.y + 1);
          }
        });
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().color(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        line.getStyle().setSVGProperty(SVGConstants.SVG_STROKE_ATTRIBUTE, value.toCssColor());
      }
    }));
  }
}