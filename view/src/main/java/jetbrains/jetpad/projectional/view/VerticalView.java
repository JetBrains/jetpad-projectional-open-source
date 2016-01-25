/*
 * Copyright 2012-2016 JetBrains s.r.o
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
import jetbrains.jetpad.model.property.ReadableProperty;

public class VerticalView extends View {
  public static final ViewPropertySpec<Boolean> INDENT = new ViewPropertySpec<>("indent", ViewPropertyKind.RELAYOUT, false);
  private static final ViewPropertySpec<Integer> INDENT_WIDTH = new ViewPropertySpec<>("indentWidth", ViewPropertyKind.NONE, 0);

  public Property<Boolean> indent() {
    return getProp(INDENT);
  }

  public ReadableProperty<Integer> indentWidth() {
    return getProp(INDENT_WIDTH);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Vector origin = ctx.origin();
    int width = 0;
    int height = 0;
    int indentWidth = indent().get() ? container().peer().textWidth(TextView.DEFAULT_FONT, "x") * 2 : 0;
    getProp(INDENT_WIDTH).set(indentWidth);

    for (View child : children()) {
      if (!child.visible().get()) continue;

      Rectangle bounds = child.bounds().get();
      child.moveTo(origin.add(new Vector(indentWidth, height)));

      height += bounds.dimension.y;
      width = Math.max(width, bounds.dimension.x + indentWidth);
    }

    int baseLine = 0;
    for (View child : children()) {
      if (!child.visible().get()) continue;
      baseLine = child.baseLine();
      break;
    }

    ctx.bounds(new Vector(width, height), baseLine);
  }
}