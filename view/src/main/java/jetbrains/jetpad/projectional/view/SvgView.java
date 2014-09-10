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

import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.svg.*;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

public class SvgView extends View {
  public static final ViewPropertySpec<SvgSvgElement> SVG_ROOT = new ViewPropertySpec<>("svgRoot", ViewPropertyKind.RELAYOUT);

  public final SvgContainer svgContainer;

  public SvgView(SvgSvgElement root) {
    root().set(root);
    svgContainer = new SvgContainer(root);
    root().addHandler(new EventHandler<PropertyChangeEvent<SvgSvgElement>>() {
      @Override
      public void onEvent(PropertyChangeEvent<SvgSvgElement> event) {
        svgContainer.root().set(event.getNewValue());
        invalidate();
      }
    });
    svgContainer.addListener(new SvgContainerAdapter() {
      @Override
      public void onAttributeSet(SvgElement element, SvgAttributeEvent event) {
        if (element == root().get() &&
            (event.getAttrSpec().toString().equalsIgnoreCase("height") || (event.getAttrSpec().toString().equalsIgnoreCase("width")))){
          invalidate();
        }
        repaint();
      }

      @Override
      public void onNodeAttached(SvgNode node) {
        repaint();
      }

      @Override
      public void onNodeDetached(SvgNode node) {
        repaint();
      }
    });
  }

  public Property<SvgSvgElement> root() {
    return getProp(SVG_ROOT);
  }

  private MouseEvent translateMouseEvent(MouseEvent e) {
    return new MouseEvent(e.x() - bounds().get().origin.x, e.y() - bounds().get().origin.y);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Vector bounds = new Vector((int) Math.ceil(root().get().width().get()),
        (int) Math.ceil(root().get().height().get()));
    ctx.bounds(bounds, baseLine());
  }
}