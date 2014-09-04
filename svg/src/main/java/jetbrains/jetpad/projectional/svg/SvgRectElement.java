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

public class SvgRectElement extends SvgStylableElement {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");
  private static final SvgAttributeSpec<Double> HEIGHT = SvgAttributeSpec.createSpec("height");
  private static final SvgAttributeSpec<Double> WIDTH = SvgAttributeSpec.createSpec("width");

  public SvgRectElement() {
    super();
  }

  public SvgRectElement(Double x, Double y, Double height, Double width) {
    this();

    setAttribute(X, x);
    setAttribute(Y, y);
    setAttribute(HEIGHT, height);
    setAttribute(WIDTH, width);
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
}