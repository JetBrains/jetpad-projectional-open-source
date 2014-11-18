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
  public static enum PointerEvents {
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

    private String myAttributeString;

    private PointerEvents(String attributeString) {
      myAttributeString = attributeString;
    }

    @Override
    public String toString() {
      return myAttributeString;
    }
  }

  public static enum Visibility {
    VISIBLE("visible"),
    HIDDEN("hidden"),
    COLLAPSE("collapse"),
    INHERIT("inherit");

    private String myAttrString;

    private Visibility(String attrString) {
      myAttrString = attrString;
    }

    @Override
    public String toString() {
      return myAttrString;
    }
  }

  private static final SvgAttributeSpec<PointerEvents> POINTER_EVENTS = SvgAttributeSpec.createSpec("pointer-events");
  private static final SvgAttributeSpec<Double> OPACITY = SvgAttributeSpec.createSpec("opacity");
  private static final SvgAttributeSpec<Visibility> VISIBILITY = SvgAttributeSpec.createSpec("visibility");
  private static final SvgAttributeSpec<SvgIRI> CLIP_PATH = SvgAttributeSpec.createSpec("clip-path");

  public Property<PointerEvents> pointerEvents() {
    return getAttribute(POINTER_EVENTS);
  }

  public Property<Double> opacity() {
    return getAttribute(OPACITY);
  }

  public Property<Visibility> visibility() {
    return getAttribute(VISIBILITY);
  }

  public Property<SvgIRI> clipPath() {
    return getAttribute(CLIP_PATH);
  }
}
