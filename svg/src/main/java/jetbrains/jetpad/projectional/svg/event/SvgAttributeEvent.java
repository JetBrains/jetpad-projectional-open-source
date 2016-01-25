/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.svg.event;

import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.projectional.svg.SvgAttributeSpec;

public class SvgAttributeEvent<ValueT> extends Event {
  private SvgAttributeSpec<ValueT> mySpec;
  private ValueT myOldValue;
  private ValueT myNewValue;

  public SvgAttributeEvent(SvgAttributeSpec<ValueT> spec, ValueT oldValue, ValueT newValue) {
    mySpec = spec;
    myOldValue = oldValue;
    myNewValue = newValue;
  }

  public SvgAttributeSpec<ValueT> getAttrSpec() {
    return mySpec;
  }

  public ValueT getOldValue() {
    return myOldValue;
  }

  public ValueT getNewValue() {
    return myNewValue;
  }
}