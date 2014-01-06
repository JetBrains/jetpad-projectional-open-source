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
package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.projectional.view.View;

class PopupPositionUpdaters {

  static PopupPositionUpdater bottomUpdater() {
    return new PopupPositionUpdater() {
      @Override
      public void update(Rectangle targetRect, Rectangle visibleRect, View popup) {
        jetbrains.jetpad.geometry.Vector target = targetRect.origin.add(new jetbrains.jetpad.geometry.Vector(0, targetRect.dimension.y));
        popup.moveTo(target);

        boolean bottom = visibleRect.contains(popup.bounds().get());

        if (!bottom) {
          popup.move(new jetbrains.jetpad.geometry.Vector(0, -targetRect.dimension.y - popup.bounds().get().dimension.y));
        }
      }
    };
  }

  static PopupPositionUpdater frontUpdater() {
    return new PopupPositionUpdater() {
      @Override
      public void update(Rectangle targetRect, Rectangle visibleRect, View popup) {
        popup.moveTo(targetRect.origin);
      }
    };
  }

  static PopupPositionUpdater leftUpdater() {
    return new PopupPositionUpdater() {
      @Override
      public void update(Rectangle targetRect, Rectangle visibleRect, View popup) {
        popup.moveTo(targetRect.origin.add(new jetbrains.jetpad.geometry.Vector(-popup.bounds().get().dimension.x, 0)));
      }
    };
  }

  static PopupPositionUpdater rightUpdater() {
    return new PopupPositionUpdater() {
      @Override
      public void update(Rectangle targetRect, Rectangle visibleRect, View popup) {
        popup.moveTo(targetRect.origin.add(new jetbrains.jetpad.geometry.Vector(targetRect.dimension.x, 0)));
      }
    };
  }
}