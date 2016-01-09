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
package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;

public interface RoleHybridSynchronizerSpec<ItemT> {
  ItemT createEmptyItem();
  boolean isEmptyItem(ItemT item);
  Cell createItemTarget();
  Cell findEditedInItemTarget(Cell itemTarget);
  HybridEditorSpec<ItemT> createHybridEditorSpecForItem(ItemT item);
  void afterRoleSynchronizerCreated(ProjectionalRoleSynchronizer<?, Property<ItemT>> sync);
  void afterHybridSynchronizerCreated(HybridSynchronizer<ItemT> sync);
}
