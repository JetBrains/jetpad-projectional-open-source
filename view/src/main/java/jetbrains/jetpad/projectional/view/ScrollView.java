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
import jetbrains.jetpad.model.property.Property;

public class ScrollView extends View {
  public static final ViewPropertySpec<Vector> MAX_DIMENSION = new ViewPropertySpec<>("maxDimension", ViewPropertyKind.RELAYOUT, Vector.ZERO);
  public static final ViewPropertySpec<Vector> OFFSET = new ViewPropertySpec<>("offset", ViewPropertyKind.RELAYOUT_AND_REPAINT, Vector.ZERO);
  public static final ViewPropertySpec<Boolean> SCROLL = new ViewPropertySpec<>("scroll", ViewPropertyKind.RELAYOUT_AND_REPAINT, false);

  private boolean myVerticalScroller;
  private boolean myHorizontalScroller;
  private Vector myInternalsBounds;

  public ScrollView() {
  }

  public Property<Vector> maxDimension() {
    return prop(MAX_DIMENSION);
  }

  public Property<Vector> offset() {
    return prop(OFFSET);
  }

  public Property<Boolean> scroll() {
    return prop(SCROLL);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    if (children().size() > 1) {
      throw new IllegalStateException();
    }

    View child = children().isEmpty() ? null : children().get(0);
    if (child != null) {
      if (scroll().get()) {
        Vector dim = child.bounds().get().dimension;
        Vector maxDim = maxDimension().get();
        Vector offset = offset().get();
        child.moveTo(ctx.origin().add(offset));

        myInternalsBounds = dim;

        Vector bounds = dim.min(maxDim);

        myVerticalScroller = dim.y > maxDim.y;
        myHorizontalScroller = dim.x > maxDim.x;

        bounds = bounds.add(new Vector(myVerticalScroller ? yScrollWidth() : 0, myHorizontalScroller ? xScrollWidth() : 0));
        ctx.bounds(bounds, 0);
      } else {
        child.moveTo(ctx.origin());
        ctx.bounds(child.bounds().get(), child.baseLine());

        myInternalsBounds = Vector.ZERO;

        myVerticalScroller = false;
        myHorizontalScroller = false;
      }
    } else {
      ctx.bounds(Vector.ZERO, 0);
    }
  }

  void scrollTo(View descendant) {
    validate();

    Rectangle db = descendant.bounds().get();
    Rectangle b = bounds().get();
    if (b.contains(db)) return;

    Vector delta = new Vector(
      ScrollUtil.moveDelta(b.xRange(), db.xRange()),
      ScrollUtil.moveDelta(b.yRange(), db.yRange())
    );

    offset().set(offset().get().add(delta));
  }

  public boolean isVerticalScroller() {
    return myVerticalScroller;
  }

  public boolean isHorizontalScroller() {
    return myHorizontalScroller;
  }

  public int xScrollWidth() {
    return 5;
  }

  public int yScrollWidth() {
    return 5;
  }

  public Vector internalsBounds() {
    return myInternalsBounds;
  }
}