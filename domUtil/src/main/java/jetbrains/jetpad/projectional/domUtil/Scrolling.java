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
    Rectangle elBounds = new Rectangle(0, 0, element.getScrollWidth(), element.getScrollHeight());
    if (!elBounds.contains(rect)) {
      throw new IllegalArgumentException(elBounds + " should contain " + rect);
    }
    adjustScrollers(rect, element);
    Rectangle visibleArea = new Rectangle(getScrollX(), getScrollY(), getScrollWidth(), getScrollHeight());
    Rectangle elementBounds = getBounds(element);
    Rectangle bounds = new Rectangle(elementBounds.origin.add(rect.origin), rect.dimension);
    if (!visibleArea.contains(bounds)) {    // are we sure about this?
      int top = element.getAbsoluteTop() + rect.origin.y;
      int left = element.getAbsoluteLeft() + rect.origin.x;
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
    int width = element.getScrollWidth();
    int height = element.getScrollHeight();
    return new Rectangle(x, y, width, height);
  }

  private static void adjustScrollers(Rectangle rect, Element element) {
    int left = element.getAbsoluteLeft() + rect.origin.x;
    int top = element.getAbsoluteTop() + rect.origin.y;
    int width = rect.dimension.x;
    int height = rect.dimension.y;

    while (element.getParentElement() != null) {
      Element parent = element.getParentElement();

      String overflow = $(parent).css("overflow");
      if ("scroll".equals(overflow) || "auto".equals(overflow)) {
        int scrollTop = parent.getScrollTop();
        int parentTop = parent.getAbsoluteTop();
        int parentHeight = parent.getClientHeight();
        int scrollLeft = parent.getScrollLeft();
        int parentLeft = parent.getAbsoluteLeft();
        int parentWidth = parent.getClientWidth();

        if (top < parentTop) {
          int delta = parentTop - top;
          parent.setScrollTop(scrollTop - delta);
          top += delta;
        } else if (top + height > parentTop + parentHeight) {
          int delta = (parentTop + parentHeight) - (top + height);
          parent.setScrollTop(scrollTop - delta);
          top += delta;
        }

        if (left < parentLeft) {
          int delta = parentLeft + parentWidth - (left + width);
          parent.setScrollLeft(scrollLeft - delta);
          left += delta;
        } else if (left + width > parentLeft + parentWidth) {
          int delta = (parentLeft + parentWidth) - (left + width);
          parent.setScrollLeft(scrollLeft - delta);
          left += delta;
        }
      }

      element = parent;
    }
  }

  private static native void log(String text) /*-{
    $wnd.console.log(text);
  }-*/;

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