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
package jetbrains.jetpad.projectional.demo.diagramExpr.mapper;

import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.cell.view.CellView;
import jetbrains.jetpad.projectional.view.VerticalView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;
import jetbrains.jetpad.values.Color;

class DiagramNodeView extends VerticalView {
  static final ViewPropertySpec<Integer> PADDING = new ViewPropertySpec<>("padding", ViewPropertyKind.RELAYOUT, 20);

  final CellView cellView;

  DiagramNodeView() {
    children().add(cellView = new CellView());
    RootController.install(cellView.container);
    cellView.background().set(Color.WHITE);
  }

  Property<Integer> padding() {
    return getProp(PADDING);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    int padding = padding().get();
    for (View child : children()) {
      child.move(new Vector(padding, padding));
    }

    Rectangle bounds = bounds().get();
    ctx.bounds(bounds.dimension.add(new Vector(2 * padding, 2 * padding)), 0);
  }
}