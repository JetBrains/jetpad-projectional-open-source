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
package jetbrains.jetpad.projectional.domUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.geometry.Rectangle;

import static com.google.gwt.query.client.GQuery.$;

public class Scrolling {
  public static void scrollTo(Rectangle rect, Element element) {
    Rectangle elBounds = new Rectangle(0, 0, element.getOffsetWidth(), element.getOffsetHeight());
    if (!elBounds.contains(rect)) {
      throw new IllegalArgumentException();
    }
    adjustScrollers(rect, element);
    Rectangle visibleArea = new Rectangle(getScrollX(), getScrollY(), getScrollWidth(), getScrollHeight());
    Rectangle bounds = getBounds(element);
    if (!visibleArea.contains(bounds)) {
      int top = element.getAbsoluteTop() + rect.origin.x;
      int left = element.getAbsoluteLeft() + rect.origin.y;
      int width = rect.dimension.x;
      int height = rect.dimension.y;

      int winTop = getScrollY();
      int winLeft = getScrollX();
      int winWidth = getScrollWidth();
      int winHeigh = getScrollHeight();

      int x = winLeft;
      int y = winTop;
      if (top < winTop) {
        y = top;
      }
      if (left < winLeft) {
        if (left + width < winWidth) {
          x = 0;
        } else {
          x = left;
        }
      }
      if (top + height > winTop + winHeigh) {
        y = top + height - winHeigh;
      }
      if (left + width > winLeft + winWidth) {
        x = left + width - winWidth;
      }
      Window.scrollTo(x, y);
    }
  }

  private static Rectangle getBounds(Element element) {
    int x = element.getAbsoluteLeft();
    int y = element.getAbsoluteTop();
    int width = element.getAbsoluteRight() - element.getAbsoluteLeft();
    int height = element.getAbsoluteBottom() - element.getAbsoluteTop();
    return new Rectangle(x, y, width, height);
  }

  private static void adjustScrollers(Rectangle rect, Element element) {
    int left = element.getOffsetLeft() + rect.origin.x;
    int top = element.getOffsetTop() + rect.origin.y;
    int width = rect.dimension.x;
    int height = rect.dimension.y;

    while (element.getParentElement() != null) {
      Element parent = element.getParentElement();
      Element offsetParent = element.getOffsetParent();

      String overflow = $(parent).css("overflow");
      if ("scroll".equals(overflow) || "auto".equals(overflow)) {
        int parentTop = parent.getScrollTop();
        int parentLeft = parent.getScrollLeft();
        int clientWidth = parent.getClientWidth();
        int clientHeight = parent.getClientHeight();

        if (top < parentTop) {
          parent.setScrollTop(top);
        }
        if (left < parentLeft) {
          if (left + width < clientWidth) {
            parent.setScrollLeft(0);
          } else {
            parent.setScrollLeft(left);
          }
        }
        if (top + height > parentTop + clientHeight) {
          parent.setScrollTop(top + height - clientHeight);
        }
        if (left + width > parentLeft + clientWidth) {
          parent.setScrollLeft(left + width - clientWidth);
        }
      }

      if (parent == offsetParent) {
        top += parent.getOffsetTop() - parent.getScrollTop();
        left += parent.getOffsetLeft() - parent.getScrollLeft();
      }
      element = parent;
    }
  }

  private static native int getScrollX() /*-{
    return $wnd.pageXOffset;
  }-*/;

  private static native int getScrollY() /*-{
    return $wnd.pageYOffset;
  }-*/;

  private static native int getScrollWidth() /*-{
    return $wnd.innerWidth;
  }-*/;

  private static native int getScrollHeight() /*-{
    return $wnd.innerHeight;
  }-*/;
}