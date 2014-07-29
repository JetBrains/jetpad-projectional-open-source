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
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.dom.DomView;

class ViewMapperFactory {
  //todo set to false because it causes ui corruption when changing zoom in chrome
  private static boolean BROWSER_LAYOUT_COLLECTIONS = false;

  static MapperFactory<View, Element> factory(final ViewToDomContext ctx) {
    return new MapperFactory<View, Element>() {
      @Override
      public Mapper<? extends View, ? extends Element> createMapper(View source) {
        Mapper<? extends View, ? extends Element> result;
        if (source instanceof TextView) {
          result = new TextViewMapper(ctx, (TextView) source);
        } else if (source instanceof LineView) {
          result = new LineViewMapper(ctx, (LineView) source);
        } else if (source instanceof MultiPointView) {
          result = new MultiPointViewMapper(ctx, (MultiPointView) source);
        } else if (BROWSER_LAYOUT_COLLECTIONS && source.getClass() == VerticalView.class) {
          result = new VerticalViewMapper(ctx, (VerticalView) source);
        } else if (BROWSER_LAYOUT_COLLECTIONS && source.getClass() == HorizontalView.class) {
          result = new HorizontalViewMapper(ctx, (HorizontalView) source);
        } else if (source instanceof ScrollView) {
          result = new ScrollViewMapper(ctx, (ScrollView) source);
        } else if (source instanceof EllipseView) {
          result = new EllipseViewMapper(ctx, (EllipseView) source);
        } else if (source instanceof ImageView) {
          result = new ImageViewMapper(ctx, (ImageView) source);
        } else if (source instanceof DomView) {
          result = new DomViewMapper(ctx, (DomView) source);
        } else if (source instanceof SvgView) {
          result = new SvgViewMapper(ctx, (SvgView) source);
        } else {
          result = new CompositeViewMapper<View, Element>(ctx, source, DOM.createDiv());
        }

        if (source instanceof VerticalView) {
          result.getTarget().addClassName("V");
        }

        if (source instanceof HorizontalView) {
          result.getTarget().addClassName("H");
        }

        if (source instanceof TextView) {
          result.getTarget().addClassName("T");
        }

        if (source instanceof ScrollView) {
          result.getTarget().addClassName("S");
        }

        return result;
      }
    };
  }
}