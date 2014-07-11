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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.CompositeEventSource;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.EllipseView;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGPathSegList;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;
import org.vectomatic.dom.svg.utils.SVGConstants;

class EllipseViewMapper extends BaseViewMapper<EllipseView, Element> {
  EllipseViewMapper(ViewToDomContext ctx, EllipseView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    OMSVGDocument doc = OMSVGParser.createDocument();
    final OMSVGSVGElement svg = createSVG(doc);
    getTarget().appendChild(svg.getElement());

    final OMSVGPathElement fillPath = new OMSVGPathElement();
    svg.appendChild(fillPath);

    final OMSVGPathElement borderPath = new OMSVGPathElement();
    svg.appendChild(borderPath);
    borderPath.getStyle().setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, "transparent");


    conf.add(GwtViewSynchronizers.svgBoundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forEventSource(new CompositeEventSource<>(getSource().center(), getSource().radius(), getSource().from(), getSource().to()), new Runnable() {
      @Override
      public void run() {
        updatePath(fillPath, false);
        updatePath(borderPath, true);
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().background(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        fillPath.getStyle().setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, value.toCssColor());
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().borderWidth(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        if (value == 0) {
          borderPath.getStyle().setVisibility(Style.Visibility.HIDDEN);
        } else {
          borderPath.getStyle().setVisibility(Style.Visibility.VISIBLE);
          borderPath.getStyle().setSVGProperty(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "" + value + "px");
        }
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().borderColor(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        borderPath.getStyle().setSVGProperty(SVGConstants.SVG_STROKE_ATTRIBUTE, value.toCssColor());
      }
    }));
  }

  private void updatePath(OMSVGPathElement path, boolean border) {
    OMSVGPathSegList segList = path.getPathSegList();
    segList.clear();
    Vector radius = getSource().radius().get();

    double from = getSource().from().get();
    double to = getSource().to().get();
    double eps = 0.0001;

    Integer borderWidth = getSource().borderWidth().get();
    int dx = borderWidth / 2;
    int dy = borderWidth / 2;

    if (Math.abs(2 * Math.PI - (to - from)) < eps) {
      segList.appendItem(path.createSVGPathSegMovetoAbs(radius.x + dx, dy));
      segList.appendItem(path.createSVGPathSegArcAbs(radius.x + dx, 2 * radius.y + dy, radius.x, radius.y, 180, false, true));
      segList.appendItem(path.createSVGPathSegArcAbs(radius.x + dx, dy, radius.x, radius.y, 180, false, true));
    } else {
      float sx = (float) (1 + Math.cos(-to)) * radius.x;
      float sy = (float) (1 + Math.sin(-to)) * radius.y;
      float tx = (float) (1 + Math.cos(-from)) * radius.x;
      float ty = (float) (1 + Math.sin(-from)) * radius.y;

      if (!border) {
        segList.appendItem(path.createSVGPathSegMovetoAbs(radius.x + dx, radius.y + dy));
        segList.appendItem(path.createSVGPathSegLinetoAbs(sx + dx, sy + dy));
      } else {
        segList.appendItem(path.createSVGPathSegMovetoAbs(sx + dx, sy + dy));
      }

      segList.appendItem(path.createSVGPathSegArcAbs(tx + dx, ty + dy, radius.x, radius.y, 0, (to - from) >= Math.PI, true));

      if (!border) {
        segList.appendItem(path.createSVGPathSegLinetoAbs(radius.x + dx, radius.y + dy));
      }
    }
  }

  @Override
  protected boolean isCustomBackgroundSync() {
    return true;
  }
}