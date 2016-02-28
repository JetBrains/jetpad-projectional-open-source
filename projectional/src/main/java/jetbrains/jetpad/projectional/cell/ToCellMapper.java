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
package jetbrains.jetpad.projectional.cell;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.composite.HasParent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ToCellMapper<SourceT, MappingSourceT extends HasParent<?>, TargetT extends Cell>
    extends Mapper<SourceT, TargetT>
    implements ToCellMapping<MappingSourceT> {

  private List<ToCellMapping<?>> myCellMappings = null;

  public ToCellMapper(SourceT source, TargetT target) {
    super(source, target);
  }

  protected void doRegisterSynchronizers(SynchronizersConfiguration conf) {}

  protected abstract MappingSourceT getCellMappingSource(SourceT source);
  protected abstract boolean isOfMappingSourceType(Object obj);

  @Override
  protected final void registerSynchronizers(final SynchronizersConfiguration conf) {
    SynchronizersConfiguration confWrapper = new SynchronizersConfiguration() {
      @Override
      public void add(Synchronizer sync) {
        ToCellMapper.this.beforeRegister(sync);
        conf.add(sync);
      }
    };
    doRegisterSynchronizers(confWrapper);
  }

  private void beforeRegister(Synchronizer sync) {
    if (sync instanceof ToCellMapping) {
      if (myCellMappings == null) {
        myCellMappings = new ArrayList<>(1);
      }
      myCellMappings.add((ToCellMapping) sync);
    }
  }

  @Override
  public MappingSourceT getSource(Cell cell) {
    if (getTarget() == cell) {
      return getCellMappingSource(getSource());
    }
    if (myCellMappings != null) {
      for (ToCellMapping<?> mapping : myCellMappings) {
        Object source = mapping.getSource(cell);
        if (source != null && isOfMappingSourceType(source)) {
          return (MappingSourceT) source;
        }
      }
    }
    return getCellMappingSource(getSource());
  }

  @Override
  public List<Cell> getCells(Object source) {
    if (getCellMappingSource(getSource()) == source) {
      return Collections.singletonList((Cell) getTarget());
    }
    List<Cell> result = null;
    if (myCellMappings != null) {
      for (ToCellMapping<?> mapping : myCellMappings) {
        List<Cell> cells = mapping.getCells(source);
        if (!cells.isEmpty()) {
          if (result == null) {
            result = new ArrayList<>(cells.size());
          }
          result.addAll(cells);
        }
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }
}
