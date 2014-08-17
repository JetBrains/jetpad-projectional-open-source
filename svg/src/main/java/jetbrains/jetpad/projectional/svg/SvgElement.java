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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class SvgElement extends SvgNode {
  private AttrMap myXmlAttributes = new AttrMap();
  protected static Map<String, SvgAttrSpec<?>> myAttrInfo = new HashMap<>();

  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return myAttrInfo;
  }

  public SvgAttrSpec<?> getSpecByName(String name) {
    return getAttrInfo().get(name);
  }

  public Set<String> attrKeys() {
    return getAttrInfo().keySet();
  }

  public AttrMap xmlAttributes() {
    return myXmlAttributes;
  }

  public boolean hasXmlAttr(String name) {
    return myXmlAttributes.containsKey(name);
  }

  public Property<String> getXmlAttr(String name) {
    if (myAttrInfo.containsKey(name)) {
      throw new UnsupportedOperationException("Svg attributes should be accessed with appropriate accessor methods, not xml attrs methods");
    }
    if (myXmlAttributes != null && myXmlAttributes.containsKey(name)) {
      return myXmlAttributes.get(name);
    }
    return null;
  }

  public void setXmlAttr(String name, String value) {
    if (myAttrInfo.containsKey(name)) {
      throw new UnsupportedOperationException("Svg attributes should be accessed with appropriate accessor methods, not xml attrs methods");
    }
    // TODO: remove value when null is passed as a value
    String oldValue = myXmlAttributes.put(name, value);
    if (value != null && !value.equals(oldValue)) {
      SvgAttributeEvent event = new SvgAttributeEvent(name, oldValue, value);
      dispatch(SvgEvents.ATTRIBUTE_CHANGED, event);
      if (isAttached()) {
        container().attributeChanged(this, event);
      }
    }
  }

  public Set<String> getPresentXmlAttributesKeys() {
    if (myXmlAttributes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(myXmlAttributes.keySet());
  }
}
