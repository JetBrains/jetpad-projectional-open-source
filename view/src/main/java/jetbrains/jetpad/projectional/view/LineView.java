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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.DoubleSegment;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

public class LineView extends View {
  static final int THRESHOLD = 5;

  public static final ViewPropertySpec<Color> COLOR = new ViewPropertySpec<>("color", ViewPropertyKind.REPAINT, Color.BLACK);
  public static final ViewPropertySpec<Integer> WIDTH = new ViewPropertySpec<>("width", ViewPropertyKind.RELAYOUT_AND_REPAINT, 1);

  private static final ViewPropertySpec<Vector> START = new ViewPropertySpec<>("start", ViewPropertyKind.RELAYOUT_AND_REPAINT, Vector.ZERO);
  private static final ViewPropertySpec<Vector> END = new ViewPropertySpec<>("end", ViewPropertyKind.RELAYOUT_AND_REPAINT, Vector.ZERO);

  public Property<Vector> start() {
    return toParentOffsetProp(START);
  }

  public Property<Vector> end() {
    return toParentOffsetProp(END);
  }

  public Property<Color> color() {
    return prop(COLOR);
  }

  public Property<Integer> width() {
    return prop(WIDTH);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Vector start = start().get();
    Vector end = end().get();

    Vector min = start.min(end);
    Vector max = start.max(end);

    int width = width().get();

    Vector widthVec = new Vector(width, width);
    ctx.bounds(new Rectangle(min.sub(widthVec), max.sub(min).add(widthVec.mul(2))), 0);
  }

  @Override
  protected boolean contains(Vector loc) {
    DoubleSegment segment = new DoubleSegment(start().get().toDoubleVector(), end().get().toDoubleVector());
    double distance = segment.distance(loc.toDoubleVector());
    return bounds().get().contains(loc) && distance < THRESHOLD;
  }
}