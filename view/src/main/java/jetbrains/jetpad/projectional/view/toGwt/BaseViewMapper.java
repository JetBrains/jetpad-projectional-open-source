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
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.CompositeEventSource;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGStyle;

class BaseViewMapper<ViewT extends View, ElementT extends Element> extends Mapper<ViewT, ElementT> {
  private ViewToDomContext myContext;

  BaseViewMapper(ViewToDomContext ctx, ViewT source, ElementT target) {
    super(source, target);
    myContext = ctx;

    if (source == source.container().decorationRoot()) {
      //we use optimization to position horizontal and vertical items in a special way (we use browser positioning)
      //that's (probably) why stacking orded doesn't work correctly: we mix relative with absolute positioning there
      getTarget().getStyle().setZIndex(100);
    }
  }

  ViewToDomContext context() {
    return myContext;
  }

  protected boolean isDomLayout() {
    return false;
  }

  protected final boolean isDomPosition() {
    if (!(getParent() instanceof BaseViewMapper)) return false;
    return ((BaseViewMapper) getParent()).isDomLayout();
  }

  protected void whenValid(final Runnable r) {
    if (getSource().container() == null) {
      final Value<Registration> reg = new Value<>();
      reg.set(getSource().attachEvents().addHandler(new EventHandler<Object>() {
        @Override
        public void onEvent(Object event) {
          whenValid(r);
          reg.get().remove();
        }
      }));
    } else {
      getSource().container().whenValid(r);
    }
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    Style targetStyle = getTarget().getStyle();

    if (!isDomPosition()) {
      targetStyle.setPosition(Style.Position.ABSOLUTE);
    } else {
      targetStyle.setPosition(Style.Position.RELATIVE);
    }

    if (!isDomPosition() || !isDomLayout()) {
      final ReadableProperty<Rectangle> positionInParent;
      if (getParent() instanceof BaseViewMapper) {
        final BaseViewMapper<?, ?> parent = (BaseViewMapper<?, ?>) getParent();
        positionInParent = new DerivedProperty<Rectangle>(getSource().bounds(), parent.getSource().bounds()) {
          @Override
          public Rectangle get() {
            Rectangle sourceBounds = getSource().bounds().get();
            Rectangle parentSourceBounds = parent.getSource().bounds().get();
            return sourceBounds.sub(parentSourceBounds.origin);
          }
        };
      } else {
        positionInParent = getSource().bounds();
      }

      final Value<Boolean> valid = new Value<>(false);

      conf.add(Synchronizers.forEventSource(new CompositeEventSource<Object>(positionInParent, getSource().border()), new Runnable() {
        @Override
        public void run() {
          valid.set(false);
          whenValid(new Runnable() {
            @Override
            public void run() {
              if (valid.get()) return;
              final Rectangle value = positionInParent.get();
              Style style = getTarget().getStyle();

              if (!isDomPosition()) {
                style.setLeft(value.origin.x, Style.Unit.PX);
                style.setTop(value.origin.y, Style.Unit.PX);
              }

              if (!isDomLayout()) {
                int width = value.dimension.x;
                int height = value.dimension.y;

                style.setWidth(width, Style.Unit.PX);
                style.setHeight(height, Style.Unit.PX);
              }
              valid.set(true);
            }
          });
        }
      }));
    }

    if (!isCustomBackgroundSync()) {
      conf.add(Synchronizers.forPropsOneWay(getSource().background(), new WritableProperty<Color>() {
        @Override
        public void set(Color value) {
          Style style = getTarget().getStyle();
          if (value == null) {
            style.setBackgroundColor(null);
          } else {
            style.setBackgroundColor(value.toCssColor());
          }
        }
      }));
    }

    conf.add(Synchronizers.forPropsOneWay(getSource().border(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        Style style = getTarget().getStyle();
        if (value != null) {
          style.setOutlineColor(value.toCssColor());
          style.setOutlineWidth(1, Style.Unit.PX);
          style.setOutlineStyle(Style.OutlineStyle.SOLID);
        } else {
          style.clearOutlineStyle();
          style.clearOutlineColor();
          style.clearBorderWidth();
        }
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().visible(), new WritableProperty<Boolean>() {
      @Override
      public void set(final Boolean value) {
        whenValid(new Runnable() {
          @Override
          public void run() {
            getTarget().getStyle().setDisplay(value ? Style.Display.BLOCK : Style.Display.NONE);
          }
        });
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().hasShadow(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        if (value) {
          getTarget().getStyle().setProperty("boxShadow", "5px 5px 10px black");
        } else {
          getTarget().getStyle().setProperty("boxShadow", null);
        }
      }
    }));
  }

  protected OMSVGSVGElement createSVG(OMSVGDocument doc) {
    final OMSVGSVGElement svg = doc.createSVGSVGElement();
    //without setting absolute position svg element might move down for unknown reason
    OMSVGStyle style = svg.getStyle();
    style.setPosition(Style.Position.ABSOLUTE);
    style.setLeft(0, Style.Unit.PX);
    style.setTop(0, Style.Unit.PX);
    return svg;
  }

  protected boolean isCustomBackgroundSync() {
    return false;
  }
}