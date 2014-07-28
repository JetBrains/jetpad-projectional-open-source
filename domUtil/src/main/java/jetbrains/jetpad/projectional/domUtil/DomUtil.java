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
import jetbrains.jetpad.geometry.Vector;

import static com.google.gwt.query.client.GQuery.$;

public class DomUtil {
  public static Rectangle visiblePart(Element ctx) {
    return DomUtil.visiblePart(ctx, new Rectangle(0, 0, ctx.getScrollWidth(), ctx.getScrollHeight()));
  }

  public static Rectangle visiblePart(Element ctx, Rectangle rect) {
    if (ctx.getOffsetParent() == null) {
      Rectangle visibleArea = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());
      return visibleArea.intersect(rect);
    } else {
      String overflow = $(ctx).css("overflow");
      Rectangle visible;
      if ("scroll".equals(overflow) || "auto".equals(overflow)) {
        visible = new Rectangle(0, 0, ctx.getOffsetWidth(), ctx.getOffsetHeight());
        rect = rect.sub(new Vector(ctx.getScrollLeft(), ctx.getScrollTop()));
      } else {
        visible = new Rectangle(0, 0, ctx.getScrollWidth(), ctx.getScrollHeight());
      }

      Rectangle newRect = visible.intersect(rect);
      return visiblePart(ctx.getOffsetParent(), newRect.add(new Vector(ctx.getOffsetLeft(), ctx.getOffsetTop())));
    }
  }
}
