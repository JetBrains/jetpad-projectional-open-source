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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class CellAdapter implements CellListener {
  @Override
  public void onPropertyChanged(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
  }

  @Override
  public void onChildAdded(CollectionItemEvent<Cell> event) {
  }

  @Override
  public void onChildRemoved(CollectionItemEvent<Cell> event) {
  }

  @Override
  public void onParentChanged(PropertyChangeEvent<Cell> event) {
  }

  @Override
  public void onAttach(CellContainer container) {
  }

  @Override
  public void onDetach(CellContainer container) {
  }
}