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

import java.util.ArrayList;
import java.util.Collection;

public class SvgPathDataBuilder {
  public static enum Interpolation {
    LINEAR, MONOTONE
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

    private Action(char c) {
      myChar = c;
    }

    char getChar() {
      return myChar;
    }
  }

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

  private double lineSlope(DoubleVector v1, DoubleVector v2) {
    return (v2.y - v1.y) / (v2.x - v1.x);
  }

  private ArrayList<Double> finiteDifferences(ArrayList<DoubleVector> points) {
    ArrayList<Double> result = new ArrayList<>(points.size());
    Double curSlope = lineSlope(points.get(0), points.get(1));
    result.add(curSlope);

    for (int i = 1; i < points.size() - 1; ++i) {
      Double newSlope = lineSlope(points.get(i), points.get(i + 1));
      result.add((curSlope + newSlope) / 2);
      curSlope = newSlope;
    }

    result.add(curSlope);

    return result;
  }

  private void doLinearInterpolation(ArrayList<DoubleVector> points) {
    for (DoubleVector point : points) {
      lineTo(point.x, point.y);
    }
  }

  private void doHermiteInterpolation(ArrayList<DoubleVector> points, ArrayList<DoubleVector> tangents) {
    if (tangents.size() < 1 ||
        (points.size() != tangents.size() && points.size() != tangents.size() + 2)) {
      doLinearInterpolation(points);
    }

    boolean quad = (points.size() != tangents.size());
    DoubleVector initPoint = points.get(0);
    DoubleVector curPoint = points.get(1);
    DoubleVector initTangent = tangents.get(0);
    DoubleVector curTangent = initTangent;
    int pointIndex = 1;

    if (quad) {
      quadraticBezierCurveTo(points.get(1).x - tangents.get(0).x * 2 / 3, curPoint.y - initTangent.y * 2 / 3, curPoint.x, curPoint.y, true);
      initPoint = points.get(1);
      pointIndex = 2;
    }

    if (tangents.size() > 1) {
      curTangent = tangents.get(1);
      curPoint = points.get(pointIndex);
      pointIndex++;
      curveTo(initPoint.x + initTangent.x, initPoint.y + initTangent.y, curPoint.x - curTangent.x, curPoint.y - curTangent.y, curPoint.x, curPoint.y, true);

      for (int tangentIndex = 2; tangentIndex < tangents.size(); ++tangentIndex, ++pointIndex) {
        curPoint = points.get(pointIndex);
        curTangent = tangents.get(tangentIndex);
        smoothCurveTo(curPoint.x - curTangent.x, curPoint.y - curTangent.y, curPoint.x, curPoint.y);
      }
    }

    if (quad) {
      DoubleVector lastPoint = points.get(pointIndex);
      quadraticBezierCurveTo(curPoint.x + curTangent.x * 2 / 3, curPoint.y + curTangent.y * 2 / 3, lastPoint.x, lastPoint.y, true);
    }
  }

  private ArrayList<DoubleVector> monotoneTangents(ArrayList<DoubleVector> points) {
    ArrayList<Double> m = finiteDifferences(points);
    Double eps = 1e-7;

    for (int i = 0; i < points.size() - 1; ++i) {
      Double slope = lineSlope(points.get(i), points.get(i + 1));

      if (Math.abs(slope) < eps) {
        m.set(i, 0.);
        m.set(i + 1, 0.);
      } else {
        Double a = m.get(i) / slope;
        Double b = m.get(i + 1) / slope;

        Double s = a * a + b * b;
        if (s > 9) {
          s = slope * 3 / Math.sqrt(s);
          m.set(i, s * a);
          m.set(i + 1, s * b);
        }
      }
    }

    ArrayList<DoubleVector> tangents = new ArrayList<>();

    for (int i = 0; i < points.size(); ++i) {
      Double slope = (points.get(Math.min(i + 1, points.size() - 1)).x - points.get(Math.max(i - 1, 0)).x) / (6 * (1 + m.get(i) * m.get(i)));
      tangents.add(new DoubleVector(slope, m.get(i) * slope));
    }

    return tangents;
  }

  // see https://github.com/mbostock/d3/blob/master/src/svg/line.js for reference
  public SvgPathDataBuilder interpolatePoints(Collection<Double> xs, Collection<Double> ys, Interpolation interpolation) {
    // NOTE: only absolute commands will be produced

    if (xs.size() != ys.size()) {
      throw new IllegalArgumentException("Sizes of xs and ys must be equal");
    }

    ArrayList<DoubleVector> points = new ArrayList<>(xs.size());
    ArrayList<Double> xsArray = new ArrayList<>(xs);
    ArrayList<Double> ysArray = new ArrayList<>(ys);

    if (xs.size() > 0) {
      moveTo(xsArray.get(0), ysArray.get(0));
    }

    for (int i = 0; i < xs.size(); ++i) {
      points.add(new DoubleVector(xsArray.get(i), ysArray.get(i)));
    }

    switch (interpolation) {
      case LINEAR:
        doLinearInterpolation(points);
        break;
      case MONOTONE:
        if (points.size() < 3) {
          doLinearInterpolation(points);
        } else {
          doHermiteInterpolation(points, monotoneTangents(points));
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported interpolation parameter");
    }

    return this;
  }
}
