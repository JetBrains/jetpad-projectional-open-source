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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.cell.mappers.PopupPositionUpdater;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.domUtil.DomUtil;

class PopupPositioner extends PopupPositionUpdater<Element> {
  private static final int SPACING = 5;

  private CellToDomContext myContext;

  PopupPositioner(CellToDomContext context) {
    myContext = context;
  }

  @Override
  protected void updateBottom(Rectangle target, Element popup) {
    boolean hasScrollers = DomUtil.hasScrollers(myContext.rootElement);
    Rectangle visiblePart;
    if (hasScrollers) {
      visiblePart = DomUtil.visiblePart(myContext.rootElement);
    } else {
      visiblePart = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());
    }

    Rectangle childBounds = new Rectangle(target.origin, new Vector(popup.getClientWidth(), popup.getAbsoluteBottom() - popup.getAbsoluteTop()));

    boolean bottom = visiblePart.contains(childBounds.add(new Vector(0, target.dimension.y)));
    boolean top = visiblePart.contains(childBounds.sub(new Vector(0, childBounds.dimension.y)));

    if (bottom || !top) {
      Tooltip.bottom(popup);
      setPosition(popup, target.origin.x, target.origin.y + target.dimension.y + SPACING);
    } else {
      Tooltip.top(popup);
      setPosition(popup, target.origin.x, target.origin.y - childBounds.dimension.y - SPACING);
    }
  }

  @Override
  protected void updateFront(Rectangle target, Element popup) {
    setPosition(popup, target.origin.x, target.origin.y);
  }

  @Override
  protected void updateLeft(Rectangle target, Element popup) {
    setPosition(popup, target.origin.x - popup.getClientWidth(), target.origin.y);
  }

  @Override
  protected void updateRight(Rectangle target, Element popup) {
    setPosition(popup, target.origin.x + target.dimension.x, target.origin.y);
  }

  private void setPosition(Element child, int left, int top) {
    Style style = child.getStyle();
    style.setLeft(left - myContext.rootElement.getAbsoluteLeft(), Style.Unit.PX);
    style.setTop(top - myContext.rootElement.getAbsoluteTop(), Style.Unit.PX);
  }
}