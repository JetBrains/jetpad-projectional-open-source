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

public final class SvgUtils {
  static WritableProperty<Color> colorAttributeTransform(final Property<SvgColor> color, final Property<Double> opacity) {
    return new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        color.set(SvgColor.create(value));
        if (value != null && value.getAlpha() != 255) {
          opacity.set(value.getAlpha() / 255.);
        }
      }
    };
  }

  public static void transformMatrix(SvgTransformable element, double a, double b, double c, double d, double e, double f) {
    element.transform().set(new SvgTransformBuilder().matrix(a, b, c, d, e, f).build());
  }

  public static void transformTranslate(SvgTransformable element, double x, double y) {
    element.transform().set(new SvgTransformBuilder().translate(x, y).build());
  }

  public static void transformTranslate(SvgTransformable element, double x) {
    element.transform().set(new SvgTransformBuilder().translate(x).build());
  }

  public static void transformScale(SvgTransformable element, double x, double y) {
    element.transform().set(new SvgTransformBuilder().scale(x, y).build());
  }

  public static void transformScale(SvgTransformable element, double x) {
    element.transform().set(new SvgTransformBuilder().scale(x).build());
  }

  public static void transformRotate(SvgTransformable element, double a, double x, double y) {
    element.transform().set(new SvgTransformBuilder().rotate(a, x, y).build());
  }

  public static void transformRotate(SvgTransformable element, double a) {
    element.transform().set(new SvgTransformBuilder().rotate(a).build());
  }

  public static void transformSkewX(SvgTransformable element, double a) {
    element.transform().set(new SvgTransformBuilder().skewX(a).build());
  }

  public static void transformSkewY(SvgTransformable element, double a) {
    element.transform().set(new SvgTransformBuilder().skewY(a).build());
  }

  private SvgUtils() {}
}
