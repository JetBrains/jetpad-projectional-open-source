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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.event.ContentKind;
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.List;

/**
 * A {@link jetbrains.jetpad.mapper.RoleSynchronizer} which has the following additional functionality:
 * - completion
 * - delete handler
 * - copy paste
 * - item separation
 *
 * @param <ContextT> - type which is a context of a synchronizer. Typically, it's a source node of a mapper which
 *                  contains this synchronizer
 * @param <SourceT>  - type which is contained in the role
 */
public interface ProjectionalRoleSynchronizer<ContextT, SourceT> extends RoleSynchronizer<SourceT, Cell> {
  void setCompletion(RoleCompletion<? super ContextT, SourceT> completion);
  void setDeleteHandler(DeleteHandler handler);
  void setClipboardParameters(ContentKind<SourceT> kind, Function<SourceT, SourceT> cloner);
  void supportContentToString(Function<SourceT, String> toString);
  void supportContentListToString(Function<List<SourceT>, String> listToString);
  <ContentT> void supportContentKind(ContentKind<ContentT> kind, Function<ContentT, SourceT> fromContent);
  <ContentT> void supportListContentKind(ContentKind<ContentT> kind, Function<ContentT, List<SourceT>> fromContent);
  void setOnLastItemDeleted(Runnable action);
  void setPlaceholderText(String text);
  void disablePlaceholder();
  void setItemFactory(Supplier<SourceT> itemFactory);
  void setSeparator(Character ch);

  SourceT getFocusedItem();

  List<SourceT> getSelectedItems();
  void select(SourceT from, SourceT to);
}