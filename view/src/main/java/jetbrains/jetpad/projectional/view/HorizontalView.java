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

public class HorizontalView extends View {
  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    int width = 0;
    int aboveBaseline = 0;
    int belowBaseLine = 0;
    for (View child : children()) {
      if (!child.get(View.VISIBLE)) continue;
      aboveBaseline = Math.max(aboveBaseline, child.baseLine());
      Rectangle childBounds = child.bounds().get();
      belowBaseLine = Math.max(belowBaseLine, childBounds.dimension.y - child.baseLine());
      width += childBounds.dimension.x;
    }

    int offset = 0;
    for (View child : children()) {
      if (!child.get(View.VISIBLE)) continue;
      Vector newOrigin = ctx.origin().add(new Vector(offset, aboveBaseline - child.baseLine()));
      child.moveTo(newOrigin);
      offset += child.bounds().get().dimension.x;
    }

    ctx.bounds(new Vector(width, aboveBaseline + belowBaseLine), aboveBaseline);
  }
}