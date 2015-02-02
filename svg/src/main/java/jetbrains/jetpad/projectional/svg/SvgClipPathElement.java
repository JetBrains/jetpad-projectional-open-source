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

public class SvgClipPathElement extends SvgGraphicsElement implements SvgTransformable {
  public static enum ClipPathUnits {
    USER_SPACE_ON_USE("userSpaceOnUse"),
    OBJECT_BOUNDING_BOX("objectBoundingBox");

    private String myAttributeString;

    private ClipPathUnits(String attributeString) {
      myAttributeString = attributeString;
    }


    @Override
    public String toString() {
      return myAttributeString;
    }
  }

  private static final SvgAttributeSpec<ClipPathUnits> CLIP_PATH_UNITS = SvgAttributeSpec.createSpec("clipPathUnits");

  @Override
  public String getElementName() {
    return "clipPath";
  }

  public Property<ClipPathUnits> clipPathUnits() {
    return getAttribute(CLIP_PATH_UNITS);
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