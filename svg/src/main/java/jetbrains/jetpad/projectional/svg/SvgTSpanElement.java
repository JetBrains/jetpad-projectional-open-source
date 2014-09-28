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

public class SvgTSpanElement extends SvgElement implements SvgTextContent {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");

  public SvgTSpanElement() {
    super();
  }

  public SvgTSpanElement(String text) {
    this();

    setText(text);
  }

  public SvgTSpanElement(double x, double y, String text) {
    this(text);

    setAttribute(X, x);
    setAttribute(Y, y);
  }

  @Override
  public String getElementName() {
    return "tspan";
  }

  public Property<Double> x() {
    return getAttribute(X);
  }

  public Property<Double> y() {
    return getAttribute(Y);
  }

  public void setText(String text) {
    children().clear();
    addText(text);
  }

  public void addText(String text) {
    SvgTextNode node = new SvgTextNode(text);
    children().add(node);
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
  public double getComputedTextLength() {
    return container().getPeer().getComputedTextLength(this);
  }
}
