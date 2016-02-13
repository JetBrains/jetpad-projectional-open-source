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
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.LineView;
import jetbrains.jetpad.values.Color;

class LineViewMapper extends BaseViewMapper<LineView, Element> {
  LineViewMapper(ViewToDomContext ctx, LineView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    Element svg = createSVG();
    getTarget().appendChild(svg);

    final Element line = SvgUtil.createSvgElement("line");
    svg.appendChild(line);

    conf.add(GwtViewSynchronizers.boundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forPropsOneWay(new DerivedProperty<Vector>(getSource().bounds(), getSource().start()) {
      @Override
      public Vector doGet() {
        return getSource().start().get().sub(getSource().bounds().get().origin);
      }
    }, new WritableProperty<Vector>() {
      @Override
      public void set(final Vector value) {
        whenValid(new Runnable() {
          @Override
          public void run() {
            line.setAttribute("x1", "" + (value.x + 1));
            line.setAttribute("y1", "" + (value.y + 1));
          }
        });
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(new DerivedProperty<Vector>(getSource().bounds(), getSource().start()) {
      @Override
      public Vector doGet() {
        return getSource().end().get().sub(getSource().bounds().get().origin);
      }
    }, new WritableProperty<Vector>() {
      @Override
      public void set(final Vector value) {
        whenValid(new Runnable() {
          @Override
          public void run() {
            line.setAttribute("x2", "" + (value.x + 1));
            line.setAttribute("y2", "" + (value.y + 1));
          }
        });
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().color(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        line.getStyle().setProperty("stroke", value.toCssColor());
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().width(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        line.getStyle().setProperty("strokeWidth", "" + value);
      }
    }));
  }
}