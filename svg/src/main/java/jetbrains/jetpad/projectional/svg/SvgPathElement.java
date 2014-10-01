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
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.values.Color;

public class SvgPathElement extends SvgGraphicsElement implements SvgTransformable, SvgShape {
  private static final SvgAttributeSpec<SvgPathData> D = SvgAttributeSpec.createSpec("d");

  public SvgPathElement() {
    super();
  }

  public SvgPathElement(SvgPathData d) {
    this();

    setAttribute(D, d);
  }

  @Override
  public String getElementName() {
    return "line";
  }

  public Property<SvgPathData> d() {
    return getAttribute(D);
  }

  @Override
  public Property<SvgTransform> transform() {
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