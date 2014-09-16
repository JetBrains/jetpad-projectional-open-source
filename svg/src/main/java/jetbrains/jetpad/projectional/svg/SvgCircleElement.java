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

public class SvgCircleElement extends SvgGraphicsElement implements SvgTransformable, SvgShape {
  private static final SvgAttributeSpec<Double> CX = SvgAttributeSpec.createSpec("cx");
  private static final SvgAttributeSpec<Double> CY = SvgAttributeSpec.createSpec("cy");
  private static final SvgAttributeSpec<Double> R = SvgAttributeSpec.createSpec("r");

  public SvgCircleElement() {
    super();
  }

  public SvgCircleElement(double cx, double cy, double r) {
    this();

    setAttribute(CX, cx);
    setAttribute(CY, cy);
    setAttribute(R, r);
  }

  public Property<Double> cx() {
    return getAttribute(CX);
  }

  public Property<Double> cy() {
    return getAttribute(CY);
  }

  public Property<Double> r() {
    return getAttribute(R);
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