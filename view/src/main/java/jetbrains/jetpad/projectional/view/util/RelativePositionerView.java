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
package jetbrains.jetpad.projectional.view.util;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

import java.util.Arrays;

public class RelativePositionerView extends View {
  private final ViewPropertySpec<Vector> RELATIVE_TO = new ViewPropertySpec<>("relativeTo", ViewPropertyKind.RELAYOUT, Vector.ZERO);
  private final ViewPropertySpec<HorizontalAnchor> HORIZONTAL_ANCHOR = new ViewPropertySpec<>("horizontalAnchor", ViewPropertyKind.RELAYOUT, HorizontalAnchor.LEFT);
  private final ViewPropertySpec<VerticalAnchor> VERTICAL_ANCHOR = new ViewPropertySpec<>("verticalAnchor", ViewPropertyKind.RELAYOUT, VerticalAnchor.BASELINE);

  public RelativePositionerView(View... views) {
    children().addAll(Arrays.asList(views));
  }

  public Property<Vector> relativeTo() {
    return toParentOffsetProp(RELATIVE_TO);
  }

  public Property<HorizontalAnchor> horizontalAnchor() {
    return getProp(HORIZONTAL_ANCHOR);
  }

  public Property<VerticalAnchor> verticalAnchor() {
    return getProp(VERTICAL_ANCHOR);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Vector relativeTo = relativeTo().get();
    HorizontalAnchor hAnchor = horizontalAnchor().get();
    VerticalAnchor vAnchor = verticalAnchor().get();

    for (View child : children()) {
      Rectangle childBounds = child.bounds().get();
      int x = 0;
      int y = 0;

      if (hAnchor == HorizontalAnchor.LEFT) {
        x = 0;
      } else if (hAnchor == HorizontalAnchor.RIGHT) {
        x = -childBounds.dimension.x;
      } else if (hAnchor == HorizontalAnchor.CENTER) {
        x = -childBounds.dimension.x / 2;
      }

      if (vAnchor == VerticalAnchor.BASELINE) {
        y = child.baseLine();
      } else if (vAnchor == VerticalAnchor.TOP) {
        y = 0;
      } else if (vAnchor == VerticalAnchor.BOTTOM) {
        y = -childBounds.dimension.y;
      } else if (vAnchor == VerticalAnchor.CENTER) {
        y = -childBounds.dimension.y / 2;
      }

      child.moveTo(relativeTo.add(new Vector(x, y)));
    }

    Rectangle bounds = null;
    for (View child  : children()) {
      if (bounds == null) {
        bounds = child.bounds().get();
      } else {
        bounds = bounds.union(child.bounds().get());
      }
    }

    ctx.bounds(bounds != null ? bounds : new Rectangle(Vector.ZERO, Vector.ZERO), 0);
  }

  public static enum HorizontalAnchor {
    LEFT, RIGHT, CENTER
  }

  public static enum VerticalAnchor {
    TOP, BOTTOM, BASELINE, CENTER
  }
}