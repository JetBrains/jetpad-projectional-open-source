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
package jetbrains.jetpad.projectional.diagram.view.layout;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.diagram.view.DiagramViewSpecs;
import jetbrains.jetpad.projectional.view.GroupView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

public class CenterVerticalLayoutView extends GroupView {
  private static final ViewPropertySpec<Rectangle> OUTER_BOUNDS = new ViewPropertySpec<>("outerBounds", ViewPropertyKind.RELAYOUT, new Rectangle(Vector.ZERO, Vector.ZERO));

  private final boolean myUseOuterBounds;

  public CenterVerticalLayoutView() {
    this(true);
  }

  public CenterVerticalLayoutView(boolean useOuterBounds) {
    myUseOuterBounds = useOuterBounds;
    if (useOuterBounds) {
      prop(DiagramViewSpecs.CONTENT_RECT_HANDLER).set(new Handler<Rectangle>() {
        @Override
        public void handle(Rectangle item) {
          outerBounds().set(item);
        }
      });
    }
  }

  private Property<Rectangle> outerBounds() {
    return prop(OUTER_BOUNDS);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Rectangle bounds;
    if (myUseOuterBounds) {
      bounds = outerBounds().get();
    } else {
      int width = 0;
      int height = 0;
      Vector origin = bounds().get().origin;
      for (View childView: children()) {
        if (!childView.visible().get()) continue;
        width = Math.max(width, childView.bounds().get().dimension.x);
        height += childView.bounds().get().dimension.y;
      }
      bounds = new Rectangle(origin, new Vector(width, height));
    }

    int yOffset = bounds.origin.y;
    for (View childView: children()) {
      if (!childView.visible().get()) continue;
      Rectangle childBounds = childView.bounds().get();
      int childWidth = childBounds.dimension.x;
      childView.moveTo(new Vector(Math.max(bounds.origin.x + (bounds.dimension.x - childWidth) / 2, 0), yOffset));
      yOffset += childBounds.dimension.y;
    }
    ctx.bounds(bounds, baseLine());
  }
}