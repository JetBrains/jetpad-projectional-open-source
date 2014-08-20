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

import jetbrains.jetpad.model.property.Property;

import java.util.HashMap;
import java.util.Map;

public class SvgRectElement extends SvgStylableElement {
  protected static Map<String, SvgAttrSpec<?>> ourAttrInfo = new HashMap<>(SvgStylableElement.ourAttrInfo);

  public static final SvgAttrSpec<Double> X = SvgAttrSpec.initAttrSpec("x", ourAttrInfo);
  public static final SvgAttrSpec<Double> Y = SvgAttrSpec.initAttrSpec("y", ourAttrInfo);
  public static final SvgAttrSpec<Double> HEIGHT = SvgAttrSpec.initAttrSpec("height", ourAttrInfo);
  public static final SvgAttrSpec<Double> WIDTH = SvgAttrSpec.initAttrSpec("width", ourAttrInfo);

  public SvgRectElement() {
    super();
  }

  public SvgRectElement(Double x, Double y, Double height, Double width) {
    this();

    setAttr(X, x);
    setAttr(Y, y);
    setAttr(HEIGHT, height);
    setAttr(WIDTH, width);
  }

  @Override
  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return ourAttrInfo;
  }

  public Property<Double> getX() {
    return getAttr(X);
  }

  public Property<Double> getY() {
    return getAttr(Y);
  }

  public Property<Double> getHeight() {
    return getAttr(HEIGHT);
  }

  public Property<Double> getWidth() {
    return getAttr(WIDTH);
  }
}
