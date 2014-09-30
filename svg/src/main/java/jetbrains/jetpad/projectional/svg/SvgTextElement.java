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

public class SvgTextElement extends SvgGraphicsElement implements SvgTransformable, SvgTextContent {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");

  public SvgTextElement() {
    super();
  }

  public SvgTextElement(String content) {
    this();

    setTextNode(content);
  }

  public SvgTextElement(double x, double y, String content) {
    this();

    setAttribute(X, x);
    setAttribute(Y, y);
    setTextNode(content);
  }

  @Override
  public String getElementName() {
    return "text";
  }

  public Property<Double> x() {
    return getAttribute(X);
  }

  public Property<Double> y() {
    return getAttribute(Y);
  }

  @Override
  public Property<SvgTransform> transform() {
    return getAttribute(TRANSFORM);
  }

  public void setTextNode(String text) {
    children().clear();
    addTextNode(text);
  }

  public void addTextNode(String text) {
    SvgTextNode textNode = new SvgTextNode(text);
    children().add(textNode);
  }

  public void setTSpan(SvgTSpanElement tspan) {
    children().clear();
    addTSpan(tspan);
  }

  public void setTSpan(String text) {
    children().clear();
    addTSpan(text);
  }

  public void addTSpan(SvgTSpanElement tspan) {
    children().add(tspan);
  }

  public void addTSpan(String text) {
    children().add(new SvgTSpanElement(text));
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