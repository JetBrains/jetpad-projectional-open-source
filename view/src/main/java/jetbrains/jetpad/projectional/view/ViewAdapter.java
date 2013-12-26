/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

class ViewAdapter implements ViewListener {
  @Override
  public void onPropertySet(ViewPropertySpec<?> prop, PropertyChangeEvent<?> event) {
  }

  @Override
  public void onCustomViewFeatureChange(CustomViewFeatureSpec spec) {
  }

  @Override
  public void onViewValidated() {
  }

  @Override
  public void onViewInvalidated() {
  }

  @Override
  public void onViewAttached() {
  }

  @Override
  public void onViewDetached() {
  }

  @Override
  public void onBoundsChanged(PropertyChangeEvent<Rectangle> change) {
  }

  @Override
  public void onToRootDeltaChanged(PropertyChangeEvent<Vector> change) {
  }

  @Override
  public void onChildAdded(CollectionItemEvent<View> event) {
  }

  @Override
  public void onChildRemoved(CollectionItemEvent<View> event) {
  }

  @Override
  public void onParentChanged(PropertyChangeEvent<View> event) {
  }
}