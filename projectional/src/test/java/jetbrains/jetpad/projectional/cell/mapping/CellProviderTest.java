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

import com.google.common.base.Predicate;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.mapper.*;
import jetbrains.jetpad.model.collections.list.ObservableCollections;
import jetbrains.jetpad.model.composite.HasParent;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CellProviderTest {
  private List<List<String>> rootSource;
  private Cell rootCell;
  private Mapper<?, ?> rootMapper;
  private CellProvider provider;

  @Before
  public void init() {
    rootSource = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d", "e"));
    rootCell = new VerticalCell();
    rootMapper = new RootMapper(rootSource, rootCell);

    MappingContext ctx = new MappingContext();
    ctx.put(ByTargetIndex.KEY, new ByTargetIndex(ctx));
    rootMapper.attachRoot(ctx);

    provider = CellProvider.fromMapper(rootMapper);
  }

  @Test
  public void attach() {
    assertEquals(rootSource, provider.getSource(rootCell));
    assertEquals(Arrays.asList(rootCell), provider.getCells(rootSource));
  }

  @Test
  public void nonAttached() {
    assertTrue(provider.getCells(new TestRoot()).isEmpty());
    assertTrue(provider.getCells(new Object()).isEmpty());
  }

  @Test
  public void nullSource() {
    assertTrue(provider.getCells(null).isEmpty());
  }

  @Test
  public void emptyPath() {
    assertTrue(provider.getCellsOnPath(Collections.emptyList()).isEmpty());
  }

  @Test
  public void predicate() {
    Cell c = rootCell.children().get(1).children().get(0);
    assertEquals("c", c.get(TextCell.TEXT));

    Object source = provider.getSource(c);
    assertEquals("c", source);

    Object filteredSource = provider.getSource(c, new Predicate<Object>() {
      @Override
      public boolean apply(Object input) {
        return !(input instanceof String);
      }
    });
    assertEquals(Arrays.asList("c", "d", "e"), filteredSource);
  }

  private static class RootMapper extends Mapper<List<List<String>>, Cell> {
    public RootMapper(List<List<String>> source, Cell target) {
      super(source, target);
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      conf.add(Synchronizers.forObservableRole(this,
        ObservableCollections.toObservable(getSource()),
        getTarget().children(),
        SetMapper.FACTORY));
    }
  }

  private static class SetMapper extends Mapper<List<String>, Cell> {
    private static final MapperFactory<List<String>, Cell> FACTORY =
      new MapperFactory<List<String>, Cell>() {
        @Override
        public Mapper<? extends List<String>, ? extends Cell> createMapper(List<String> source) {
          return new SetMapper(source, new HorizontalCell());
        }
      };

    public SetMapper(List<String> source, Cell target) {
      super(source, target);
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      conf.add(Synchronizers.forObservableRole(this,
        ObservableCollections.toObservable(getSource()),
        getTarget().children(),
        StringMapper.FACTORY));
    }
  }

  private static class StringMapper extends Mapper<String, Cell> {
    public static final MapperFactory<String, Cell> FACTORY =
      new MapperFactory<String, Cell>() {
        @Override
        public Mapper<? extends String, ? extends Cell> createMapper(String source) {
          return new StringMapper(source, new TextCell());
        }
      };

    public StringMapper(String source, Cell target) {
      super(source, target);
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      conf.add(Synchronizers.forRegistration(
        getTarget().set(TextCell.TEXT, getSource())));
    }
  }

  private static class TestRoot implements HasParent<TestRoot> {
    @Override
    public TestRoot getParent() {
      return null;
    }
  }
}
