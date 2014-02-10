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

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.view.View;

class CellToViewContext {
  private View myRootView;
  private View myTargetView;
  private View myPopupView;
  private Property<Boolean> myContainerFocused = new ValueProperty<>(false);

  CellToViewContext(View rootView, View targetView, View popupView) {
    myRootView = rootView;
    myTargetView = targetView;
    myPopupView = popupView;
  }

  View rootView() {
    return myRootView;
  }

  View targetView() {
    return myTargetView;
  }

  View popupView() {
    return myPopupView;
  }

  Property<Boolean> containerFocused() {
    return myContainerFocused;
  }
}