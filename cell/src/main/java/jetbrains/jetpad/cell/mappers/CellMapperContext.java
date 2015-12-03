/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.cell.mappers;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.HashMap;
import java.util.Map;

public class CellMapperContext<TargetT> {
  public final TargetT rootElement;
  public final Property<Boolean> focused = new ValueProperty<>(false);

  private final Map<TargetT, CellMapper<? extends Cell, TargetT>> myMappers = new HashMap<>();

  protected CellMapperContext(TargetT rootElement) {
    this.rootElement = rootElement;
  }

  void register(CellMapper<? extends Cell, TargetT> mapper) {
    if (myMappers.get(mapper.getTarget()) != null) {
      throw new IllegalStateException();
    }
    myMappers.put(mapper.getTarget(), mapper);
  }

  void unregister(CellMapper<? extends Cell, TargetT> mapper) {
    CellMapper<?, TargetT> result = myMappers.remove(mapper.getTarget());
    if (result != mapper) {
      throw new IllegalStateException();
    }
  }

  public CellMapper<? extends Cell, TargetT> findMapper(TargetT e) {
    return myMappers.get(e);
  }
}
