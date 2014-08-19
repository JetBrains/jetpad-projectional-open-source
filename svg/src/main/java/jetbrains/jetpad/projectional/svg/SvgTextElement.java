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

import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;

import java.util.HashMap;
import java.util.Map;

public class SvgTextElement extends SvgStylableElement {
  protected static Map<String, SvgAttrSpec<?>> myAttrInfo = new HashMap<>(SvgStylableElement.myAttrInfo);

  public static final SvgAttrSpec<Double> X = SvgAttrSpec.initAttrSpec("x", myAttrInfo);
  public static final SvgAttrSpec<Double> Y = SvgAttrSpec.initAttrSpec("y", myAttrInfo);

  @Override
  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return myAttrInfo;
  }

  public Property<Double> getX() {
    return getAttr(X);
  }

  public Property<Double> getY() {
    return getAttr(Y);
  }

  public void addTextNode(String text) {
    ObservableList<SvgNode> children = children();
    SvgTextNode textNode = new SvgTextNode(text);
    children.add(textNode);
  }
}
