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
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.values.Color;

public interface SvgTextContent {
  static final SvgAttributeSpec<SvgColor> FILL = SvgAttributeSpec.createSpec("fill");
  static final SvgAttributeSpec<SvgColor> STROKE = SvgAttributeSpec.createSpec("stroke");
  static final SvgAttributeSpec<Double> FILL_OPACITY = SvgAttributeSpec.createSpec("fill-opacity");
  static final SvgAttributeSpec<Double> STROKE_OPACITY = SvgAttributeSpec.createSpec("stroke-opacity");

  public Property<SvgColor> fill();

  public WritableProperty<Color> fillColor();

  public Property<Double> fillOpacity();

  public Property<SvgColor> stroke();

  public WritableProperty<Color> strokeColor();

  public Property<Double> strokeOpacity();
}
