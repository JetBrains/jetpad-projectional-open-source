/*
 * Copyright 2012-2015 JetBrains s.r.o
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
import jetbrains.jetpad.projectional.view.View;

public class GroupView extends View {
  @Override
  protected boolean contains(Vector loc) {
    return false;
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    boolean empty = true;
    Rectangle bounds = null;
    for (View child : children()) {
      if (!child.visible().get()) continue;
      Rectangle childBounds = child.bounds().get();
      bounds = bounds == null ? childBounds : bounds.union(childBounds);
      empty = false;
    }

    visible().set(!empty);
    if (empty) {
      ctx.bounds(new Rectangle(Vector.ZERO, Vector.ZERO), 0);
    } else {
      ctx.bounds(bounds, 0);
    }
  }
}