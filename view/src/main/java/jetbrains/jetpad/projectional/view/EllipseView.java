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

import com.google.common.collect.Range;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

public class EllipseView extends View {
  private static final ViewPropertySpec<Vector> CENTER = new ViewPropertySpec<>("center", ViewPropertyKind.RELAYOUT, new Vector(0, 0));
  private static final ViewPropertySpec<Double> FROM = new ViewPropertySpec<>("from", ViewPropertyKind.RELAYOUT, 0.0);
  private static final ViewPropertySpec<Double> TO = new ViewPropertySpec<>("to", ViewPropertyKind.RELAYOUT, 2 * Math.PI);

  public static final ViewPropertySpec<Vector> RADIUS = new ViewPropertySpec<>("dimension", ViewPropertyKind.RELAYOUT, new Vector(10, 10));
  public static final ViewPropertySpec<Color> BORDER_COLOR = new ViewPropertySpec<>("color", ViewPropertyKind.REPAINT, Color.BLACK);
  public static final ViewPropertySpec<Integer> BORDER_WIDTH = new ViewPropertySpec<>("borderWidth", ViewPropertyKind.RELAYOUT_AND_REPAINT, 0);


  public EllipseView() {
    background().set(Color.BLACK);
  }

  public Property<Vector> radius() {
    return getProp(RADIUS);
  }

  public Property<Vector> center() {
    return toParentOffsetProp(CENTER);
  }

  public Property<Double> from() {
    return angleProperty(FROM);
  }

  public Property<Double> to() {
    return angleProperty(TO);
  }

  public Property<Color> borderColor() {
    return getProp(BORDER_COLOR);
  }

  public Property<Integer> borderWidth() {
    return getProp(BORDER_WIDTH);
  }

  private Property<Double> angleProperty(ViewPropertySpec<Double> spec) {
    final Property<Double> prop = getProp(spec);

    return new Property<Double>() {
      @Override
      public String getPropExpr() {
        return prop.getPropExpr();
      }

      @Override
      public Double get() {
        return prop.get();
      }

      @Override
      public void set(Double value) {
        if (value < -2 * Math.PI || value > 2 * Math.PI) {
          throw new IllegalArgumentException();
        }
        prop.set(value);
      }

      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Double>> handler) {
        return prop.addHandler(handler);
      }
    };
  }

  @Override
  public boolean contains(Vector loc) {
    Vector r = radius().get();
    Vector nl = loc.sub(center().get());
    double eps = 0.001;
    if (nl.length() <= eps) return true;
    if (r.x * r.x * nl.y * nl.y + r.y * r.y * nl.x * nl.x > r.x * r.x * r.y * r.y) return false;

    double phi = Math.atan2(nl.y, nl.x);
    double from = from().get();
    double to = to().get();

    for (int i = -1 ; i <= 1; i++) {
      if (Range.closed(from + 2 * Math.PI * i, to + 2 * Math.PI * i).contains(phi)) return true;
    }

    return false;
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Integer borderWidth = borderWidth().get();
    Vector radius = radius().get().add(new Vector(borderWidth / 2, borderWidth / 2));
    Rectangle rect = new Rectangle(center().get().sub(radius), radius.mul(2));

    ctx.bounds(rect, 0);
  }
}