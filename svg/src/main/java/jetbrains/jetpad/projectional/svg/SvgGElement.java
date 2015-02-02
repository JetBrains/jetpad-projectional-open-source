/*
 * Copyright 2012-2015 JetBrains s.r.o
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

public class SvgGElement extends SvgGraphicsElement implements SvgTransformable, SvgContainer {
  @Override
  public String getElementName() {
    return "g";
  }

  @Override
  public Property<SvgTransform> transform() {
    return getAttribute(TRANSFORM);
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