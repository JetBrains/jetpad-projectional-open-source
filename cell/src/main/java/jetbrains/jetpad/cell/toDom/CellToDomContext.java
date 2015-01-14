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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Element;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.HashMap;
import java.util.Map;

class CellToDomContext {
  final Property<Boolean> focused = new ValueProperty<>(false);
  final Element rootElement;
  final Element focusElement;
  private final Map<Element, BaseCellMapper<?>> myMappers = new HashMap<>();

  CellToDomContext(Element rootElement) {
    this.rootElement = rootElement;
    this.focusElement = rootElement;
  }

  void register(BaseCellMapper<?> mapper) {
    if (myMappers.get(mapper.getTarget()) != null) {
      throw new IllegalStateException();
    }

    myMappers.put(mapper.getTarget(), mapper);
  }

  void unregister(BaseCellMapper<?> mapper) {
    BaseCellMapper<?> result = myMappers.remove(mapper.getTarget());
    if (result != mapper) {
      throw new IllegalStateException();
    }
  }

  BaseCellMapper<?> findMapper(Element e) {
    return myMappers.get(e);
  }
}