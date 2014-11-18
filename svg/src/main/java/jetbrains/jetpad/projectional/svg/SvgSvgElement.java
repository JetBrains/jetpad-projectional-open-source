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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.geometry.DoubleRectangle;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.event.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

public class SvgSvgElement extends SvgStylableElement implements SvgContainer, SvgLocatable {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");
  private static final SvgAttributeSpec<Double> WIDTH = SvgAttributeSpec.createSpec("width");
  private static final SvgAttributeSpec<Double> HEIGHT = SvgAttributeSpec.createSpec("height");
  private static final SvgAttributeSpec<ViewBoxRectangle> VIEW_BOX = SvgAttributeSpec.createSpec("viewBox");

  public SvgSvgElement() {
    super();
  }

  public SvgSvgElement(double width, double height) {
    this();

    setAttribute(WIDTH, width);
    setAttribute(HEIGHT, height);
  }

  @Override
  public String getElementName() {
    return "svg";
  }

  public void setStyle(SvgCssResource css) {
    children().add(new SvgStyleElement(css));
  }

  public Property<Double> x() {
    return getAttribute(X);
  }

  public Property<Double> y() {
    return getAttribute(Y);
  }

  public Property<Double> width() {
    return getAttribute(WIDTH);
  }

  public Property<Double> height() {
    return getAttribute(HEIGHT);
  }

  public Property<ViewBoxRectangle> viewBox() {
    return getAttribute(VIEW_BOX);
  }

  public WritableProperty<DoubleRectangle> viewBoxRect() {
    return new WritableProperty<DoubleRectangle>() {
      @Override
      public void set(DoubleRectangle value) {
        viewBox().set(new ViewBoxRectangle(value));
      }
    };
  }

  @Override
  public Property<Double> opacity() {
    return getAttribute(OPACITY);
  }

  @Override
  public Property<SvgIRI> clipPath() {
    return getAttribute(CLIP_PATH);
  }

  @Override
  public <EventT extends Event> Registration addEventHandler(SvgEventSpec spec, SvgEventHandler<EventT> handler) {
    // due to bug in lib-gwt-svg, getOwnerSvgElement throws exception on root svg element
    throw new IllegalStateException("Can't add handlers to <svg> element");
  }

  @Override
  public DoubleVector pointToTransformedCoordinates(DoubleVector point) {
    return container().getPeer().invertTransform(this, point);
  }

  @Override
  public DoubleVector pointToAbsoluteCoordinates(DoubleVector point) {
    return container().getPeer().applyTransform(this, point);
  }

  @Override
  public DoubleRectangle getBBox() {
    return container().getPeer().getBBox(this);
  }

  public static class ViewBoxRectangle {
    private double myX;
    private double myY;
    private double myWidth;
    private double myHeight;

    public ViewBoxRectangle(double x, double y, double width, double height) {
      myX = x;
      myY = y;
      myWidth = width;
      myHeight = height;
    }

    public ViewBoxRectangle(DoubleRectangle rect) {
      myX = rect.origin.x;
      myY = rect.origin.y;
      myWidth = rect.dimension.x;
      myHeight = rect.dimension.y;
    }

    public ViewBoxRectangle(Rectangle rect) {
      myX = rect.origin.x;
      myY = rect.origin.y;
      myWidth = rect.dimension.x;
      myHeight = rect.dimension.y;
    }

    @Override
    public String toString() {
      return myX + " " + myY + " " + myWidth + " " + myHeight;
    }
  }
}