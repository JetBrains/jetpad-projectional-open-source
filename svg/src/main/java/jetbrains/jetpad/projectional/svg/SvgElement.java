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
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

import java.util.Collections;
import java.util.Set;

public class SvgElement extends SvgNode {
  private AttrMap myAttributes = new AttrMap();

  public AttrMap attributes() {
    return myAttributes;
  }

  public boolean hasAttr(String name) {
    return myAttributes.containsKey(name);
  }

  public Property<String> getAttr(String name) {
    if (myAttributes != null && myAttributes.containsKey(name)) {
      return myAttributes.get(name);
    }
    return null;
  }

  public void setAttr(String name, String value) {
    // TODO: remove value when null is passed as a value
    String oldValue = myAttributes.put(name, value);
    if (value != null && !value.equals(oldValue)) {
      SvgAttributeEvent event = new SvgAttributeEvent(name, oldValue, value);
      dispatch(SvgEvents.ATTRIBUTE_CHANGED, event);
      if (isAttached()) {
        container().attributeChanged(this, event);
      }
    }
  }

  public Set<String> getAttributesKeys() {
    if (myAttributes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(myAttributes.keySet());
  }
}
