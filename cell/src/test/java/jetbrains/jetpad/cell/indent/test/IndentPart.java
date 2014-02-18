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
package jetbrains.jetpad.cell.indent.test;

import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.composite.Composite;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.List;

public class IndentPart extends HasParent<IndentPart, IndentPart> implements Composite<IndentPart> {
  private boolean myIndented;
  private boolean myAttached;
  private boolean myVisible = true;

  public final ObservableList<IndentPart> children = new ChildList<IndentPart, IndentPart>(this) {
    @Override
    protected void afterItemAdded(int index, IndentPart item, boolean success) {
      super.afterItemAdded(index, item, success);
      if (!success || indentCell() == null) return;

      indentCell().childAdded(item);
    }

    @Override
    protected void beforeItemRemoved(int index, IndentPart item) {
      super.beforeItemRemoved(index, item);
      if (indentCell() == null) return;

      indentCell().childRemoved(item);
    }
  };

  public IndentPart() {
    this(false);
  }

  public IndentPart(boolean indented) {
    myIndented = indented;
  }

  public boolean isVisible() {
    return myVisible;
  }

  public void setVisible(boolean visible) {
    if (myVisible == visible) return;
    myVisible = visible;
    if (indentCell() != null) {
      indentCell().visibilityChanged(this, new PropertyChangeEvent<>(!visible, visible));
    }
  }

  public boolean isIndented() {
    return myIndented;
  }

  @Override
  public ObservableList<IndentPart> children() {
    return children;
  }

  IndentCell indentCell() {
    if (parent().get() != null) {
      return parent().get().indentCell();
    }
    return null;
  }

  boolean isAttached() {
    return myAttached;
  }

  void setAttached(boolean attached) {
    myAttached = attached;
  }
}