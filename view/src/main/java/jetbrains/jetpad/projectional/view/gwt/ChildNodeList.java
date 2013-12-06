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
package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

import java.util.AbstractList;

class ChildNodeList extends AbstractList<Node> {
  private final Element myParent;

  ChildNodeList(Element parent) {
    myParent = parent;
  }

  @Override
  public Node get(int index) {
    return myParent.getChild(index);
  }

  @Override
  public Node set(int index, Node element) {
    if (element.getParentElement() != null) throw new IllegalStateException();

    Node child = get(index);
    myParent.replaceChild(child, element);
    return child;
  }

  @Override
  public void add(int index, Node element) {
    if (element.getParentElement() != null) throw new IllegalStateException();

    if (index == 0) {
      myParent.insertFirst(element);
    } else {
      Node prev = myParent.getChild(index - 1);
      myParent.insertAfter(element, prev);
    }
  }

  @Override
  public Node remove(int index) {
    Node child = myParent.getChild(index);
    myParent.removeChild(child);
    return child;
  }

  @Override
  public int size() {
    return myParent.getChildCount();
  }
}