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

public class SvgCircleElement extends SvgStylableElement {
  private static final SvgAttrSpec<Double> CX = SvgAttrSpec.createSpec("cx");
  private static final SvgAttrSpec<Double> CY = SvgAttrSpec.createSpec("cy");
  private static final SvgAttrSpec<Double> R = SvgAttrSpec.createSpec("r");

  public SvgCircleElement() {
    super();
  }

  public SvgCircleElement(Double cx, Double cy, Double r) {
    this();

    setAttr(CX, cx);
    setAttr(CY, cy);
    setAttr(R, r);
  }

  public Property<Double> cx() {
    return getAttr(CX);
  }

  public Property<Double> cy() {
    return getAttr(CY);
  }

  public Property<Double> r() {
    return getAttr(R);
  }
}
