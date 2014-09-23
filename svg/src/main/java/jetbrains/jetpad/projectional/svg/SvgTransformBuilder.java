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

import jetbrains.jetpad.geometry.DoubleVector;

public class SvgTransformBuilder {
  private StringBuilder myStringBuilder = new StringBuilder();

  public String build() {
    return myStringBuilder.toString();
  }

  private SvgTransformBuilder addTransformation(String name, double... values) {
    myStringBuilder.append(name).append('(');
    for (double val : values) {
      myStringBuilder.append(val).append(' ');
    }
    myStringBuilder.append(") ");
    return this;
  }

  public SvgTransformBuilder matrix(double a, double b, double c, double d, double e, double f) {
    return addTransformation("matrix", a, b, c, d, e, f);
  }

  public SvgTransformBuilder translate(double x, double y) {
    return addTransformation("translate", x, y);
  }

  public SvgTransformBuilder translate(DoubleVector vector) {
    return translate(vector.x, vector.y);
  }

  public SvgTransformBuilder translate(double x) {
    return addTransformation("translate", x);
  }

  public SvgTransformBuilder scale(double x, double y) {
    return addTransformation("scale", x, y);
  }

  public SvgTransformBuilder scale(double x) {
    return addTransformation("scale", x);
  }

  public SvgTransformBuilder rotate(double a, double x, double y) {
    return addTransformation("rotate", a, x, y);
  }

  public SvgTransformBuilder rotate(double a) {
    return addTransformation("rotate", a);
  }

  public SvgTransformBuilder skewX(double a) {
    return addTransformation("skewX", a);
  }

  public SvgTransformBuilder skewY(double a) {
    return addTransformation("skewY", a);
  }
}