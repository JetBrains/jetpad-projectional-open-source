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
package jetbrains.jetpad.projectional.cell.mapping;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.ByTargetIndex;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.composite.HasParent;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CellProviders {

  public static CellProvider fromMapper(Mapper<?, ?> mapper) {
    MappingContext mappingContext = mapper.getMappingContext();
    if (!mappingContext.contains(ByTargetIndex.KEY)) {
      mappingContext.put(ByTargetIndex.KEY, new ByTargetIndex(mappingContext));
    }
    return new CellProviderImpl(mapper);
  }

  public static SimpleCellProvider simpleProvider(Mapper<?, ?> mapper) {
    final CellProvider provider = fromMapper(mapper);
    return new SimpleCellProvider() {
      @Override
      public List<Cell> getCells(final HasParent<?> source) {
        return provider.getCells(source, new Iterator<Object>() {
          private HasParent<?> myCurrent = source;

          @Override
          public boolean hasNext() {
            return myCurrent != null;
          }

          @Override
          public Object next() {
            if (myCurrent == null) {
              throw new NoSuchElementException();
            }
            HasParent<?> current = myCurrent;
            myCurrent = myCurrent.getParent();
            return current;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        });
      }

      @Override
      public Object getSource(Cell cell) {
        return provider.getSource(cell);
      }
    };
  }
}
