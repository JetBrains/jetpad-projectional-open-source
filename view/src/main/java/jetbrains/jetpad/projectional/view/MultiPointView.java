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

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class MultiPointView extends View {
  public static final CustomViewFeatureSpec POINTS = new CustomViewFeatureSpec("points");
  public static final ViewPropertySpec<Color> COLOR = new ViewPropertySpec<>("color", ViewPropertyKind.REPAINT, Color.BLACK);
  public static final ViewPropertySpec<Integer> WIDTH = new ViewPropertySpec<>("width", ViewPropertyKind.RELAYOUT_AND_REPAINT, 1);

  public final List<Vector> points;

  protected MultiPointView() {
    points = new PointList();
  }

  public Property<Color> color() {
    return prop(COLOR);
  }

  public Property<Integer> width() {
    return prop(WIDTH);
  }

  protected Rectangle calculateBounds() {
    Vector min = null;
    Vector max = null;

    for (Vector p : points) {
      min = min == null ? p : min.min(p);
      max = max == null ? p : max.max(p);
    }

    if (min == null) {
      return null;
    } else {
      return new Rectangle(min, max.sub(min).add(new Vector(1, 1)));
    }
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Rectangle bounds = calculateBounds();
    if (bounds == null) {
      ctx.bounds(Vector.ZERO, 0);
    } else {
      int width = width().get();
      Vector widthVec = new Vector(width, width);
      ctx.bounds(new Rectangle(bounds.origin.sub(widthVec.div(2)), bounds.dimension.add(widthVec)), 0);
    }
  }

  private class PointList extends AbstractList<Vector> {
    private List<Vector> myRelativePoints = new ArrayList<>();

    @Override
    public Vector get(int index) {
      return myRelativePoints.get(index).add(toRootDelta().get());
    }

    @Override
    public int size() {
      return myRelativePoints.size();
    }

    @Override
    public Vector set(int index, Vector element) {
      Vector result = remove(index);
      add(index, element);
      return result;
    }

    @Override
    public void add(int index, Vector element) {
      Vector relative = element.add(toRootDelta().get());
      myRelativePoints.add(index, relative);
      invalidate();
      firePointsChange();
      repaint();
    }

    @Override
    public Vector remove(int index) {
      Vector result = get(index);
      myRelativePoints.remove(index);
      repaint();
      invalidate();
      firePointsChange();
      return result;
    }

    private void firePointsChange() {
      fire(new ListenerCaller<ViewListener>() {
        @Override
        public void call(ViewListener l) {
          l.onCustomViewFeatureChange(POINTS);
        }
      });
    }
  }
}