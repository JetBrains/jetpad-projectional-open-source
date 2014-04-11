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

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.GroupView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

public class IndentHorizontalLayoutView extends GroupView {
  private final ViewPropertySpec<Integer> PADDING = new ViewPropertySpec<>("padding", ViewPropertyKind.RELAYOUT);
  private final ViewPropertySpec<Integer> INDENT = new ViewPropertySpec<>("indent", ViewPropertyKind.RELAYOUT);

  public Property<Integer> padding() {
    return prop(PADDING);
  }

  public Property<Integer> indent() {
    return prop(INDENT);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    int xOffset = bounds().get().origin.x + indent().get();
    int prevX = bounds().get().origin.x;
    int y = bounds().get().origin.y + padding().get();
    int height = 0;
    for (View child: children()) {
      child.moveTo(new Vector(xOffset, y));
      xOffset += child.bounds().get().dimension.x + indent().get();
      height = Math.max(height, child.bounds().get().dimension.y);
    }

    ctx.bounds(new Rectangle(bounds().get().origin, new Vector(xOffset - prevX, height + 2 * padding().get())), 0);
  }
}