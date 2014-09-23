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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;


public abstract class SvgNode extends HasParent<SvgNode, SvgNode> {
  private SvgNodeContainer myContainer;

  private SvgChildList myChildren;

  public SvgNodeContainer container() {
    return myContainer;
  }

  public ObservableList<SvgNode> children() {
    if (myChildren == null) {
      myChildren = new SvgChildList(this);
    }
    return myChildren;
  }

  public boolean isAttached() {
    return myContainer != null;
  }

  void attach(SvgNodeContainer container) {
    if (isAttached()) {
      throw new IllegalStateException("Svg element is already attached");
    }

    for (SvgNode node : children()) {
      node.attach(container);
    }

    myContainer = container;
    myContainer.svgNodeAttached(this);
  }

  void detach() {
    if (!isAttached()) {
      throw new IllegalStateException("Svg element is not attached");
    }

    for (SvgNode node : children()) {
      node.detach();
    }

    myContainer.svgNodeDetached(this);
    myContainer = null;
  }

  private class SvgChildList extends ChildList<SvgNode, SvgNode> {
    public SvgChildList(SvgNode parent) {
      super(parent);
    }

    @Override
    public void add(final int index, final SvgNode node) {
      if (isAttached()) {
        node.attach(container());
      }
      super.add(index, node);
    }

    @Override
    public SvgNode remove(final int index) {
      final SvgNode node = get(index);
      if (isAttached()) {
        node.detach();
      }
      return super.remove(index);
    }
  }
}