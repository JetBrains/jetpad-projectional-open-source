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

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.HasParent;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.model.transform.Transformation;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CellProviderTest extends EditingTestCase {
  private Cell rootSource;
  private CellProvider<HasParent<?>> cellProvider;

  @Before
  public void init() {
    rootSource = new HorizontalCell();
    TestMapper rootMapper = new TestMapper(rootSource);
    rootMapper.attachRoot();
    myCellContainer.root.children().add(rootMapper.getTarget());
    cellProvider = CellProviders.fromMapper(rootMapper);
  }

  @Test
  public void findCellForIndirectSource() {
    TextCell child = new TextCell("text");
    rootSource.children().add(child);
    List<Cell> targets = cellProvider.getCells(child);
    assertEquals(1, targets.size());
    assertTrue(targets.get(0) instanceof IndirectTarget);
  }

  private static class TestMapper extends Mapper<Cell, Cell> {
    TestMapper(Cell source) {
      super(source, new HorizontalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      Transformation<ObservableList<Cell>, ObservableList<Property<Cell>>> transformation
          = Transformers.<Cell>toPropsListTwoWay().transform(getSource().children());
      ProjectionalRoleSynchronizer<Cell, Property<Cell>> sync = ProjectionalSynchronizers.forRole(
          TestMapper.this, transformation.getTarget(), getTarget(),
          new MapperFactory<Property<Cell>, Cell>() {
            @Override
            public Mapper<? extends Property<Cell>, ? extends Cell> createMapper(Property<Cell> cellAsProp) {
              return new IndirectHasParentMapper(cellAsProp);
            }
          });
      sync.setItemFactory(new Supplier<Property<Cell>>() {
        @Override
        public Property<Cell> get() {
          return new ValueProperty<Cell>(new TextCell("empty"));
        }
      });
      conf.add(sync);
      conf.add(Synchronizers.forRegistration(Registration.from(transformation)));
    }
  }

  private static class IndirectHasParentMapper extends Mapper<Property<Cell>, Cell> implements HasParentWrapper {
    IndirectHasParentMapper(Property<Cell> source) {
      super(source, new IndirectTarget());
    }

    @Override
    public HasParent<?> getCellMappingSource() {
      return getSource().get();
    }
  }

  private static class IndirectTarget extends HorizontalCell {
    IndirectTarget() {
    }
  }
}