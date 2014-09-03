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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.svg.event.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

public class SvgSvgElement extends SvgStylableElement {
  private static final SvgAttrSpec<Double> HEIGHT = SvgAttrSpec.createSpec("height");
  private static final SvgAttrSpec<Double> WIDTH = SvgAttrSpec.createSpec("width");

  public SvgSvgElement() {
    super();
  }

  public SvgSvgElement(Double height, Double width) {
    this();

    setAttr(HEIGHT, height);
    setAttr(WIDTH, width);
  }

  public Property<Double> height() {
    return getAttr(HEIGHT);
  }

  public Property<Double> width() {
    return getAttr(WIDTH);
  }

  @Override
  public <EventT extends Event> Registration addEventHandler(SvgEventSpec spec, SvgEventHandler<EventT> handler) {
    // due to bug in lib-gwt-svg, getOwnerSvgElement throws exception on root svg element
    throw new IllegalStateException("Can't add handlers to <svg> element");
  }
}
