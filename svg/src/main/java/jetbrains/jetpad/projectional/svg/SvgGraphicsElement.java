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

public abstract class SvgGraphicsElement extends SvgStylableElement {
  public static enum PointerEventsEnum {
    VISIBLE_PAINTED("visiblePainted"),
    VISIBLE_FILL("visibleFill"),
    VISIBLE_STROKE("visibleStroke"),
    VISIBLE("visible"),
    PAINTED("painted"),
    FILL("fill"),
    STROKE("stroke"),
    ALL("all"),
    NONE("none"),
    INHERIT("inherit");

    private String myAttrString;

    private PointerEventsEnum(String attrString) {
      myAttrString = attrString;
    }

    @Override
    public String toString() {
      return myAttrString;
    }
  }

  private static final SvgAttributeSpec<PointerEventsEnum> POINTER_EVENTS = SvgAttributeSpec.createSpec("pointer-events");

  public Property<PointerEventsEnum> pointerEvents() {
    return getAttribute(POINTER_EVENTS);
  }
}
