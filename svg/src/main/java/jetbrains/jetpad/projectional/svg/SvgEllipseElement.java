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

public class SvgEllipseElement extends SvgStylableElement {
  protected static Map<String, SvgAttrSpec<?>> ourAttrInfo = new HashMap<>(SvgStylableElement.ourAttrInfo);

  public static final SvgAttrSpec<Double> CX = SvgAttrSpec.initAttrSpec("cx", ourAttrInfo);
  public static final SvgAttrSpec<Double> CY = SvgAttrSpec.initAttrSpec("cy", ourAttrInfo);
  public static final SvgAttrSpec<Double> RX = SvgAttrSpec.initAttrSpec("rx", ourAttrInfo);
  public static final SvgAttrSpec<Double> RY = SvgAttrSpec.initAttrSpec("ry", ourAttrInfo);

  @Override
  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return ourAttrInfo;
  }

  public Property<Double> getCx() {
    return getAttr(CX);
  }

  public Property<Double> getCy() {
    return getAttr(CY);
  }

  public Property<Double> getRx() {
    return getAttr(RX);
  }

  public Property<Double> getRy() {
    return getAttr(RY);
  }
}
