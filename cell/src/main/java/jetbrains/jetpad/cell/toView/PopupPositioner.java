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
package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.mappersUtil.PopupPositionUpdater;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.projectional.view.View;

class PopupPositioner extends PopupPositionUpdater<View> {
  private View myParent;

  PopupPositioner(View parent) {
    myParent = parent;
  }

  @Override
  protected void updateBottom(Rectangle targetRect, View popup, boolean hasTop) {
    Vector target = targetRect.origin.add(new Vector(0, targetRect.dimension.y));
    popup.moveTo(target);
    if (hasTop) return;

    Rectangle visibleRect = myParent.container().visibleRect();
    ReadableProperty<Rectangle> popupBounds = popup.bounds();
    boolean bottom = visibleRect.contains(popupBounds.get());
    Vector delta = new Vector(0, -targetRect.dimension.y - popupBounds.get().dimension.y);
    boolean top = visibleRect.contains(popupBounds.get().add(delta));
    if (!bottom && top) {
      popup.move(delta);
    }
  }

  @Override
  protected void updateTop(Rectangle targetRect, View popup, boolean hasBottom) {
    ReadableProperty<Rectangle> popupBounds = popup.bounds();
    Vector target = targetRect.origin.add(new Vector(0, -popupBounds.get().dimension.y));
    popup.moveTo(target);
    if (hasBottom) return;

    Rectangle visibleRect = myParent.container().visibleRect();
    boolean top = visibleRect.contains(popupBounds.get());
    Vector delta = new Vector(0, targetRect.dimension.y + popupBounds.get().dimension.y);
    boolean bottom = visibleRect.contains(popupBounds.get().add(delta));
    if (!top && bottom) {
      popup.move(delta);
    }
  }

  @Override
  protected void updateFront(Rectangle target, View popup) {
    popup.moveTo(target.origin);
  }

  @Override
  protected void updateLeft(Rectangle target, View popup) {
    popup.moveTo(target.origin.add(new Vector(-popup.bounds().get().dimension.x, 0)));
  }

  @Override
  protected void updateRight(Rectangle target, View popup) {
    popup.moveTo(target.origin.add(new Vector(target.dimension.x, 0)));
  }
}