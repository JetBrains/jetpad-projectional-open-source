/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

public class LineView extends View {
  static final int THRESHOLD = 5;

  public static final ViewPropertySpec<Color> COLOR = new ViewPropertySpec<Color>("color", ViewPropertyKind.REPAINT, Color.BLACK);

  private static final ViewPropertySpec<Vector> START = new ViewPropertySpec<Vector>("start", ViewPropertyKind.RELAYOUT_AND_REPAINT, Vector.ZERO);
  private static final ViewPropertySpec<Vector> END = new ViewPropertySpec<Vector>("end", ViewPropertyKind.RELAYOUT_AND_REPAINT, Vector.ZERO);

  public Property<Vector> start() {
    return new ToParentOffsetProperty(prop(START));
  }

  public Property<Vector> end() {
    return new ToParentOffsetProperty(prop(END));
  }

  public Property<Color> color() {
    return prop(COLOR);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Vector start = start().get();
    Vector end = end().get();

    Vector min = start.min(end);
    Vector max = start.max(end);

    ctx.bounds(new Rectangle(min, max.sub(min).add(new Vector(1, 1))), 0);
  }

  @Override
  protected boolean contains(Vector loc) {
    DoubleSegment segment = new DoubleSegment(start().get().toDoubleVector(), end().get().toDoubleVector());
    double distance = segment.distance(loc.toDoubleVector());
    return bounds().get().contains(loc) && distance < THRESHOLD;
  }

  private class ToParentOffsetProperty extends DerivedProperty<Vector> implements Property<Vector> {
    private Property<Vector> myBaseProperty;

    private ToParentOffsetProperty(Property<Vector> baseProperty) {
      super(baseProperty, toRootDelta());
      myBaseProperty = baseProperty;
    }

    @Override
    public Vector get() {
      return myBaseProperty.get().add(toRootDelta().get());
    }

    @Override
    public void set(Vector value) {
      myBaseProperty.set(value.sub(toRootDelta().get()));
    }
  }
}