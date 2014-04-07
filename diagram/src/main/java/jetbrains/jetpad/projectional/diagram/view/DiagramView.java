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
package jetbrains.jetpad.projectional.diagram.view;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.projectional.view.GroupView;

public class DiagramView extends GroupView {
  private GroupView myConnectionView = new GroupView();

  public final GroupView itemsView = new GroupView();
  public final GroupView popupView = new GroupView();

  public ObservableList<Connection> connections = new ConnectionsList();

  private Registration myConnectionAttachReg;

  public DiagramView() {
    children().add(myConnectionView);
    children().add(itemsView);
    children().add(popupView);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    myConnectionView.validate();
    itemsView.validate();
    super.doValidate(ctx);
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    for (Connection c : connections) {
      c.attach();
    }

    myConnectionAttachReg = connections.addListener(new CollectionListener<Connection>() {
      @Override
      public void onItemAdded(CollectionItemEvent<Connection> event) {
        event.getItem().attach();
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<Connection> event) {
        event.getItem().detach();
      }
    });
  }

  @Override
  protected void onDetach() {
    myConnectionAttachReg.remove();

    for (Connection c : connections) {
      c.detach();
    }

    super.onDetach();
  }

  private class ConnectionsList extends ObservableArrayList<Connection> {
    @Override
    protected void afterItemAdded(int index, Connection item, boolean success) {
      super.afterItemAdded(index, item, success);
      myConnectionView.children().add(item.view());
    }

    @Override
    protected void afterItemRemoved(int index, Connection item, boolean success) {
      super.afterItemRemoved(index, item, success);
      myConnectionView.children().remove(item.view());
    }
  }
}