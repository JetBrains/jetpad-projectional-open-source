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
package jetbrains.jetpad.projectional.cell.support;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.event.ContentKind;
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.projectional.cell.Cell;

import java.util.List;

public interface ProjectionalRoleSynchronizer<ContextT, SourceT> extends RoleSynchronizer<SourceT, Cell> {
  void setCompletion(RoleCompletion<? super ContextT, SourceT> completion);
  void setDeleteHandler(DeleteHandler handler);
  void setClipboardParameters(ContentKind<SourceT> kind, Function<SourceT, SourceT> cloner);
  void setOnLastItemDeleted(CellAction action);
  void setPlaceholderText(String text);
  void setItemFactory(Supplier<SourceT> itemFactory);

  SourceT getFocusedItem();

  List<SourceT> getSelectedItems();
  void select(SourceT from, SourceT to);
}