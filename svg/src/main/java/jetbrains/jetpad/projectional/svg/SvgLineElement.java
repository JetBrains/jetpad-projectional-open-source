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

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.values.Color;

public class SvgLineElement extends SvgGraphicsElement implements SvgTransformable, SvgShape {
  private static final SvgAttributeSpec<Double> X1 = SvgAttributeSpec.createSpec("x1");
  private static final SvgAttributeSpec<Double> Y1 = SvgAttributeSpec.createSpec("y1");
  private static final SvgAttributeSpec<Double> X2 = SvgAttributeSpec.createSpec("x2");
  private static final SvgAttributeSpec<Double> Y2 = SvgAttributeSpec.createSpec("y2");

  public SvgLineElement() {
    super();
  }

  public SvgLineElement(double x1, double y1, double x2, double y2) {
    this();

    setAttribute(X1, x1);
    setAttribute(Y1, y1);
    setAttribute(X2, x2);
    setAttribute(Y2, y2);
  }

  @Override
  public String getElementName() {
    return "line";
  }

  public Property<Double> x1() {
    return getAttribute(X1);
  }

  public Property<Double> y1() {
    return getAttribute(Y1);
  }

  public Property<Double> x2() {
    return getAttribute(X2);
  }

  public Property<Double> y2() {
    return getAttribute(Y2);
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
}