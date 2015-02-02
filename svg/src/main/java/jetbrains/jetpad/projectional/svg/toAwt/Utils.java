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
package jetbrains.jetpad.projectional.svg.toAwt;

import org.w3c.dom.Node;

import java.util.AbstractList;
import java.util.List;

class Utils {
  static List<Node> elementChildren(final Node e) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return e.getChildNodes().item(index);
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        Node child = get(index);
        e.replaceChild(element, child);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        if (index == size()) {
          e.insertBefore(element, null);
        } else {
          e.insertBefore(element, get(index));
        }
      }

      @Override
      public Node remove(int index) {
        Node child = get(index);
        e.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return e.getChildNodes().getLength();
      }
    };
  }
}