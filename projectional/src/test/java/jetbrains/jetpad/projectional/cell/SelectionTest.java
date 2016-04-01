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
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.util.RootController;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers.forRole;
import static org.junit.Assert.assertFalse;

public class SelectionTest extends EditingTestCase {
  private ContainerMapper containerMapper;

  @Before
  public void setup() {
    /*
     * + container
     *   + top
     *     + middle
     *       + a
     *   + top
     *     + middle
     *       + b
     */
    Container container = new Container(new Top(new Middle(new Leaf('a'))), new Top(new Middle(new Leaf('b'))));
    containerMapper = new ContainerMapper(container);
    containerMapper.attachRoot();
    myCellContainer.root.children().add(containerMapper.getTarget());
    CellActions.toFirstFocusable(containerMapper.getTarget()).run();
    RootController.install(myCellContainer);
  }

  @Test
  public void cantSelectChildWhenParentSelected() {
    List<Object> oldSelection = getSelection();
    for ( ; ; ) {
      press(Key.RIGHT, ModifierKey.SHIFT);
      List<Object> newSelection = getSelection();
      if (newSelection.equals(oldSelection)) {
        break;
      }
      assertFalse(additionContainsDescendants(oldSelection, newSelection));
      oldSelection = newSelection;
    }
  }

  private List<Object> getSelection() {
    List<Object> selection = new ArrayList<>();
    collectSelected(containerMapper, selection);
    return selection;
  }

  private void collectSelected(WithProjectionalSync withSync, List<Object> collectHere) {
    collectHere.addAll(withSync.getSync().getSelectedItems());
    for (Mapper m : (List<Mapper>) withSync.getSync().getMappers()) {
      if (m instanceof WithProjectionalSync) {
        collectSelected((WithProjectionalSync) m, collectHere);
      }
    }
  }

  private boolean additionContainsDescendants(List<Object> older, List<Object> newer) {
    List<Object> addition = new ArrayList<>(newer);
    addition.removeAll(older);

    List<Object> same = new ArrayList<>(newer);
    same.retainAll(older);

    for (Object former : same) {
      for (Object added : addition) {
        if (isDescendant(former, added)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isDescendant(Object ancestor, Object descendant) {
    if (ancestor instanceof Top) {
      if (descendant instanceof Middle) {
        return ((Top) ancestor).middles.contains(descendant);
      } else if (descendant instanceof Leaf) {
        for (Middle m : ((Top) ancestor).middles) {
          if (isDescendant(m, descendant)) {
            return true;
          }
        }
      }
    } else if (ancestor instanceof Middle && descendant instanceof Leaf) {
      return ((Middle) ancestor).leaves.contains(descendant);
    }
    return false;
  }


  private interface WithProjectionalSync {
    ProjectionalRoleSynchronizer getSync();
  }

  private static class Container {
    private ObservableList<Top> tops = new ObservableArrayList<>();

    private Container(Top ... tops) {
      Collections.addAll(this.tops, tops);
    }
  }

  private static class ContainerMapper extends Mapper<Container, Cell> implements WithProjectionalSync {
    private ProjectionalRoleSynchronizer<Container, Top> sync;

    private ContainerMapper(Container source) {
      super(source, new HorizontalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      sync = forRole(this, getSource().tops, getTarget(), new MapperFactory<Top, Cell>() {
        @Override
        public Mapper<? extends Top, ? extends Cell> createMapper(Top top) {
          return new TopMapper(top);
        }
      });
      conf.add(sync);
    }

    @Override
    public ProjectionalRoleSynchronizer getSync() {
      return sync;
    }
  }

  private static class Top {
    private ObservableList<Middle> middles = new ObservableArrayList<>();

    private Top(Middle ... middles) {
      Collections.addAll(this.middles, middles);
    }
  }

  private static class TopMapper extends Mapper<Top, Cell> implements WithProjectionalSync {
    private ProjectionalRoleSynchronizer<Top, Middle> sync;

    private TopMapper(Top source) {
      super(source, new HorizontalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      sync = forRole(this, getSource().middles, getTarget(), new MapperFactory<Middle, Cell>() {
        @Override
        public Mapper<? extends Middle, ? extends Cell> createMapper(Middle middle) {
          return new MiddleMapper(middle);
        }
      });
      conf.add(sync);
    }

    @Override
    public ProjectionalRoleSynchronizer getSync() {
      return sync;
    }
  }

  private static class Middle {
    private ObservableList<Leaf> leaves = new ObservableArrayList<>();

    private Middle(Leaf ... leaves) {
      Collections.addAll(this.leaves, leaves);
    }
  }

  private static class MiddleMapper extends Mapper<Middle, Cell> implements WithProjectionalSync {
    private ProjectionalRoleSynchronizer<Middle, Leaf> sync;

    private MiddleMapper(Middle source) {
      super(source, new HorizontalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      sync = forRole(this, getSource().leaves, getTarget(), new MapperFactory<Leaf, Cell>() {
        @Override
        public Mapper<? extends Leaf, ? extends Cell> createMapper(Leaf leaf) {
          return new LeafMapper(leaf);
        }
      });
      conf.add(sync);
    }

    @Override
    public ProjectionalRoleSynchronizer getSync() {
      return sync;
    }
  }

  private static class Leaf {
    private char label;
    private Leaf(char label) {
      this.label = label;
    }
  }

  private static class LeafMapper extends Mapper<Leaf, TextCell> {
    private LeafMapper(Leaf source) {
      super(source, new TextCell(String.valueOf(source.label)));
      getTarget().addTrait(TextEditing.textEditing());
      getTarget().addTrait(TextEditing.textNavigation(false, true));    // Only one position allowed
    }
  }
}
