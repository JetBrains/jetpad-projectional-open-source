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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

class PopupPositioner {
  private CellToDomContext myContext;

  PopupPositioner(CellToDomContext context) {
    myContext = context;
  }

  void positionBottom(Rectangle target, Element popup) {
    Rectangle visibleRect = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());
    Rectangle childBounds = new Rectangle(target.origin, new Vector(popup.getClientWidth(), popup.getAbsoluteBottom() - popup.getAbsoluteTop()));

    if (visibleRect.contains(childBounds)) {
      setPosition(
        popup,
        target.origin.x,
        target.origin.y + target.dimension.y);
    } else {
      setPosition(
        popup,
        target.origin.x,
        target.origin.y - childBounds.dimension.y
      );
    }
  }

  void positionFront(Rectangle target, Element popup) {
    setPosition(
      popup,
      target.origin.x,
      target.origin.y);
  }

  void positionLeft(Rectangle target, Element popup) {
    setPosition(
      popup,
      target.origin.x - popup.getClientWidth(),
      target.origin.y);
  }

  public void positionRight(Rectangle target, Element popup) {
    setPosition(popup, target.origin.x + target.dimension.x, target.origin.y);
  }

  private void setPosition(Element child, int left, int top) {
    Style style = child.getStyle();
    style.setLeft(left - myContext.rootElement.getAbsoluteLeft(), Style.Unit.PX);
    style.setTop(top - myContext.rootElement.getAbsoluteTop(), Style.Unit.PX);
  }
}