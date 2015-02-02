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
package jetbrains.jetpad.projectional.diagram.util;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;

public abstract class SubList<ItemT> extends ObservableArrayList<ItemT> {
  protected abstract ObservableList<ItemT> getBaseList();

  @Override
  protected void afterItemAdded(int index, ItemT item, boolean success) {
    super.afterItemAdded(index, item, success);
    getBaseList().add(item);
  }

  @Override
  protected void afterItemRemoved(int index, ItemT item, boolean success) {
    super.afterItemRemoved(index, item, success);
    getBaseList().remove(item);
  }
}