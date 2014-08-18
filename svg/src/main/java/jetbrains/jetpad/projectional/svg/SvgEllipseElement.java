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
  public static final SvgAttrSpec<Double> CX = new SvgAttrSpec<>("cx");
  public static final SvgAttrSpec<Double> CY = new SvgAttrSpec<>("cy");
  public static final SvgAttrSpec<Double> RX = new SvgAttrSpec<>("rx");
  public static final SvgAttrSpec<Double> RY = new SvgAttrSpec<>("ry");

  protected static Map<String, SvgAttrSpec<?>> myAttrInfo;
  static {
    myAttrInfo = new HashMap<>(SvgElement.myAttrInfo);
    myAttrInfo.put(CX.toString(), CX);
    myAttrInfo.put(CY.toString(), CY);
    myAttrInfo.put(RX.toString(), RX);
    myAttrInfo.put(RY.toString(), RY);
  }

  @Override
  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return myAttrInfo;
  }

  public Property<Double> getCx() {
    return getProp(CX);
  }

  public Property<Double> getCy() {
    return getProp(CY);
  }

  public Property<Double> getRx() {
    return getProp(RX);
  }

  public Property<Double> getRy() {
    return getProp(RY);
  }
}
