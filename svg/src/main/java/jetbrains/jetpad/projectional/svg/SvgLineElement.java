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

public class SvgLineElement extends SvgStylableElement {
  private static final SvgAttrSpec<Double> X1 = SvgAttrSpec.createSpec("x1");
  private static final SvgAttrSpec<Double> Y1 = SvgAttrSpec.createSpec("y1");
  private static final SvgAttrSpec<Double> X2 = SvgAttrSpec.createSpec("x2");
  private static final SvgAttrSpec<Double> Y2 = SvgAttrSpec.createSpec("y2");

  public SvgLineElement() {
    super();
  }

  public SvgLineElement(Double x1, Double y1, Double x2, Double y2) {
    this();

    setAttr(X1, x1);
    setAttr(Y1, y1);
    setAttr(X2, x2);
    setAttr(Y2, y2);
  }

  public Property<Double> getX1() {
    return getAttr(X1);
  }

  public Property<Double> getY1() {
    return getAttr(Y1);
  }

  public Property<Double> getX2() {
    return getAttr(X2);
  }

  public Property<Double> getY2() {
    return getAttr(Y2);
  }
}
