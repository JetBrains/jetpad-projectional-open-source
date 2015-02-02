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
package jetbrains.jetpad.projectional.svg.toDom;

import org.vectomatic.dom.svg.OMNode;

import java.util.AbstractList;
import java.util.List;

class Utils {
  static List<OMNode> elementChildren(final OMNode e) {
    return new AbstractList<OMNode>() {
      @Override
      public OMNode get(int index) {
        return e.getChildNodes().getItem(index);
      }

      @Override
      public OMNode set(int index, OMNode element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        OMNode child = get(index);
        e.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, OMNode element) {
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
      public OMNode remove(int index) {
        OMNode child = get(index);
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