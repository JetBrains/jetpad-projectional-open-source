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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class SvgTextNode extends SvgNode {
  private static final ObservableList<SvgNode> NO_CHILDREN_LIST = new ObservableArrayList<SvgNode>() {
    @Override
    public void add(int index, SvgNode item) {
      throw new UnsupportedOperationException("Cannot add children to SvgTextNode");
    }

    @Override
    public SvgNode remove(int index) {
      throw new UnsupportedOperationException("Cannot remove children from SvgTextNode");
    }
  };

  private Property<String> myContent = new ValueProperty<>();

  public SvgTextNode(String text) {
    super();
    myContent.set(text);
  }

  public Property<String> textContent() {
    return myContent;
  }

  @Override
  public ObservableList<SvgNode> children() {
    return NO_CHILDREN_LIST;
  }

  @Override
  public String toString() {
    return textContent().get();
  }
}