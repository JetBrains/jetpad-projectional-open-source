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

import jetbrains.jetpad.geometry.DoubleRectangle;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.values.Color;

public class SvgRectElement extends SvgGraphicsElement implements SvgTransformable, SvgShape {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");
  private static final SvgAttributeSpec<Double> WIDTH = SvgAttributeSpec.createSpec("width");
  private static final SvgAttributeSpec<Double> HEIGHT = SvgAttributeSpec.createSpec("height");

  public SvgRectElement() {
    super();
  }

  public SvgRectElement(double x, double y, double width, double height) {
    this();

    setAttribute(X, x);
    setAttribute(Y, y);
    setAttribute(HEIGHT, height);
    setAttribute(WIDTH, width);
  }

  public SvgRectElement(Rectangle rect) {
    this(rect.origin.x, rect.origin.y, rect.dimension.x, rect.dimension.y);
  }

  public SvgRectElement(DoubleRectangle rect) {
    this(rect.origin.x, rect.origin.y, rect.dimension.x, rect.dimension.y);
  }

  @Override
  public String getElementName() {
    return "rect";
  }

  public Property<Double> x() {
    return getAttribute(X);
  }

  public Property<Double> y() {
    return getAttribute(Y);
  }

  public Property<Double> height() {
    return getAttribute(HEIGHT);
  }

  public Property<Double> width() {
    return getAttribute(WIDTH);
  }

  @Override
  public Property<String> transform() {
    return getAttribute(TRANSFORM);
  }

  @Override
  public Property<SvgColor> fill() {
    return getAttribute(FILL);
  }

  @Override
  public WritableProperty<Color> fillColor() {
    return SvgUtils.colorAttributeTransform(fill(), fillOpacity());
  }

  @Override
  public Property<Double> fillOpacity() {
    return getAttribute(FILL_OPACITY);
  }

  @Override
  public Property<SvgColor> stroke() {
    return getAttribute(STROKE);
  }

  @Override
  public WritableProperty<Color> strokeColor() {
    return SvgUtils.colorAttributeTransform(stroke(), strokeOpacity());
  }

  @Override
  public Property<Double> strokeOpacity() {
    return getAttribute(STROKE_OPACITY);
  }

  @Override
  public Property<Double> strokeWidth() {
    return getAttribute(STROKE_WIDTH);
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
}