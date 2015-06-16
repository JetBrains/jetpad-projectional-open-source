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
package jetbrains.jetpad.projectional.domUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.base.Interval;
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

      int deltaX = getScrollAdjustment(new Interval(winLeft, winLeft + winWidth), new Interval(left, left + width), winLeft);
      int deltaY = getScrollAdjustment(new Interval(winTop, winTop + winHeigh), new Interval(top, top + height), winTop);

      if (deltaX != 0 || deltaY != 0) {
        Window.scrollTo(winLeft + deltaX, winTop + deltaY);
      }
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

        int deltaX = getScrollAdjustment(new Interval(parentLeft, parentLeft + parentWidth), new Interval(left, left + width), scrollLeft);
        if (deltaX != 0) {
          parent.setScrollLeft(scrollLeft + deltaX);
          left -= deltaX;
        }
        int deltaY = getDelta(new Interval(parentTop, parentTop + parentHeight), new Interval(top, top + height));
        if (deltaY != 0) {
          parent.setScrollTop(scrollTop + deltaY);
          top -= deltaY;
        }
      }

      element = parent;
    }
  }

  private static int getScrollAdjustment(Interval vis, Interval target, int current) {
    int delta = getDelta(vis, target);
    if (current + delta < 0) {
      delta = -current;
    }
    return delta;
  }

  static int getDelta(Interval vis, Interval target) {
    if (vis.getLength() < target.getLength()) {
      return target.getLowerBound() - vis.getLowerBound();
    }

    if (target.getLowerBound() < vis.getLowerBound() || target.getUpperBound() > vis.getUpperBound()) {
      return target.getUpperBound() - vis.getUpperBound();
    }
    return 0;
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