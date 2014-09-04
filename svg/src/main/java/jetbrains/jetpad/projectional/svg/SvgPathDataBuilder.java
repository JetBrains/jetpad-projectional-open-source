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

public class SvgPathDataBuilder {
  private StringBuilder myStringBuilder;
  private boolean myDefaultAbsolute;

  public SvgPathDataBuilder(boolean defaultAbsolute) {
    myStringBuilder = new StringBuilder();
    myDefaultAbsolute = defaultAbsolute;
  }

  public SvgPathDataBuilder() {
    this(true);
  }

  public String build() {
    return myStringBuilder.toString();
  }

  // FIXME: varargs has bad performance, use verbose appends in building methods
  private void addAction(Action action, boolean absolute, double... coordinates) {
    if (absolute) {
      myStringBuilder.append(Character.toUpperCase(action.getChar()));
    } else {
      myStringBuilder.append(Character.toLowerCase(action.getChar()));
    }
    for (double coord : coordinates) {
      myStringBuilder.append(coord).append(' ');
    }
  }

  private void addActionWithStringTokens(Action action, boolean absolute, String... tokens) {
    if (absolute) {
      myStringBuilder.append(Character.toUpperCase(action.getChar()));
    } else {
      myStringBuilder.append(Character.toLowerCase(action.getChar()));
    }
    for (String token : tokens) {
      myStringBuilder.append(token).append(' ');
    }
  }

  public SvgPathDataBuilder moveTo(double x, double y, boolean absolute) {
    addAction(Action.MOVE_TO, absolute, x, y);
    return this;
  }

  public SvgPathDataBuilder moveTo(double x, double y) {
    moveTo(x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder lineTo(double x, double y, boolean absolute) {
    addAction(Action.LINE_TO, absolute, x, y);
    return this;
  }

  public SvgPathDataBuilder lineTo(double x, double y) {
    lineTo(x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder horizontalLineTo(double x, boolean absolute) {
    addAction(Action.HORIZONTAL_LINE_TO, absolute, x);
    return this;
  }

  public SvgPathDataBuilder horizontalLineTo(double x) {
    horizontalLineTo(x, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder verticalLineTo(double y, boolean absolute) {
    addAction(Action.VERTICAL_LINE_TO, absolute, y);
    return this;
  }

  public SvgPathDataBuilder verticalLineTo(double y) {
    verticalLineTo(y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder curveTo(double x1, double y1, double x2, double y2, double x, double y, boolean absolute) {
    addAction(Action.CURVE_TO, absolute, x1, y1, x2, y2, x, y);
    return this;
  }

  public SvgPathDataBuilder curveTo(double x1, double y1, double x2, double y2, double x, double y) {
    curveTo(x1, y1, x2, y2, x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder smoothCurveTo(double x2, double y2, double x, double y, boolean absolute) {
    addAction(Action.SMOOTH_CURVE_TO, absolute, x2, y2, x, y);
    return this;
  }

  public SvgPathDataBuilder smoothCurveTo(double x2, double y2, double x, double y) {
    smoothCurveTo(x2, y2, x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder quadraticBezierCurveTo(double x1, double y1, double x, double y, boolean absolute) {
    addAction(Action.QUADRATIC_BEZIER_CURVE_TO, absolute, x1, y1, x, y);
    return this;
  }

  public SvgPathDataBuilder quadraticBezierCurveTo(double x1, double y1, double x, double y) {
    quadraticBezierCurveTo(x1, y1, x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder smoothQuadraticBezierCurveTo(double x, double y, boolean absolute) {
    addAction(Action.SMOOTH_QUADRATIC_BEZIER_CURVE_TO, absolute, x, y);
    return this;
  }

  public SvgPathDataBuilder smoothQuadraticBezierCurveTo(double x, double y) {
    smoothQuadraticBezierCurveTo(x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder ellipticalArc(double rx, double ry, double xAxisRotation, boolean largeArc, boolean sweep,
                            double x, double y, boolean absolute) {
    addActionWithStringTokens(Action.ELLIPTICAL_ARC, absolute,
        Double.toString(rx), Double.toString(ry), Double.toString(xAxisRotation),
        (largeArc ? "1" : "0"), (sweep ? "1" : "0"),
        Double.toString(x), Double.toString(y));
    return this;
  }

  public SvgPathDataBuilder ellipticalArc(double rx, double ry, double xAxisRotation, boolean largeArc, boolean sweep,
                            double x, double y) {
    ellipticalArc(rx, ry, xAxisRotation, largeArc, sweep, x, y, myDefaultAbsolute);
    return this;
  }

  public SvgPathDataBuilder closePath() {
    addAction(Action.CLOSE_PATH, myDefaultAbsolute);
    return this;
  }

  private static enum Action {
    MOVE_TO('m'),
    LINE_TO('l'),
    HORIZONTAL_LINE_TO('h'),
    VERTICAL_LINE_TO('v'),
    CURVE_TO('c'),
    SMOOTH_CURVE_TO('s'),
    QUADRATIC_BEZIER_CURVE_TO('q'),
    SMOOTH_QUADRATIC_BEZIER_CURVE_TO('t'),
    ELLIPTICAL_ARC('a'),
    CLOSE_PATH('z');

    private final char myChar;

    Action(char c) {
      myChar = c;
    }

    char getChar() {
      return myChar;
    }
  }
}