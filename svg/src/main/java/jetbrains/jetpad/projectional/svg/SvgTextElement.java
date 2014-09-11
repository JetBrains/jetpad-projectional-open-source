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

public class SvgTextElement extends SvgGraphicsElement implements SvgTransformable {
  private static final SvgAttributeSpec<Double> X = SvgAttributeSpec.createSpec("x");
  private static final SvgAttributeSpec<Double> Y = SvgAttributeSpec.createSpec("y");

  public SvgTextElement() {
    super();
  }

  public SvgTextElement(String content) {
    this();

    setTextNode(content);
  }

  public SvgTextElement(Double x, Double y, String content) {
    this();

    setAttribute(X, x);
    setAttribute(Y, y);
    setTextNode(content);
  }

  public Property<Double> x() {
    return getAttribute(X);
  }

  public Property<Double> y() {
    return getAttribute(Y);
  }

  @Override
  public Property<String> transform() {
    return getAttribute(TRANSFORM);
  }

  public void setTextNode(String text) {
    children().clear();
    addTextNode(text);
  }

  public void addTextNode(String text) {
    SvgTextNode textNode = new SvgTextNode(text);
    children().add(textNode);
  }
}