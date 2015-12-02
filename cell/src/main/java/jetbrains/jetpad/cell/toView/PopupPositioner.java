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
package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.decorations.PopupPositionUpdater;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.View;

class PopupPositioner extends PopupPositionUpdater<View> {
  private View myParent;

  PopupPositioner(View parent) {
    myParent = parent;
  }

  @Override
  protected void updateBottom(Rectangle targetRect, View popup) {
    Rectangle visibleRect = myParent.container().visibleRect();

    Vector target = targetRect.origin.add(new Vector(0, targetRect.dimension.y));
    popup.moveTo(target);

    boolean bottom = visibleRect.contains(popup.bounds().get());
    Vector delta = new Vector(0, -targetRect.dimension.y - popup.bounds().get().dimension.y);
    boolean top = visibleRect.contains(popup.bounds().get().add(delta));

    if (!bottom && top) {
      popup.move(delta);
    }
  }

  @Override
  protected void updateFront(Rectangle targetRect, View popup) {
    popup.moveTo(targetRect.origin);
  }

  @Override
  protected void updateLeft(Rectangle targetRect, View popup) {
    popup.moveTo(targetRect.origin.add(new Vector(-popup.bounds().get().dimension.x, 0)));
  }

  @Override
  protected void updateRight(Rectangle targetRect, View popup) {
    popup.moveTo(targetRect.origin.add(new Vector(targetRect.dimension.x, 0)));
  }
}