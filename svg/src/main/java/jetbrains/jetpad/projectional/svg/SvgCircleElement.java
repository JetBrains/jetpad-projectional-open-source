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

public class SvgCircleElement extends SvgGraphicsElement {
  private static final SvgAttributeSpec<Double> CX = SvgAttributeSpec.createSpec("cx");
  private static final SvgAttributeSpec<Double> CY = SvgAttributeSpec.createSpec("cy");
  private static final SvgAttributeSpec<Double> R = SvgAttributeSpec.createSpec("r");

  public SvgCircleElement() {
    super();
  }

  public SvgCircleElement(Double cx, Double cy, Double r) {
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
}