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
package jetbrains.jetpad.projectional.selection;

import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.util.RootController;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.cell.util.CellFactory.*;
import static jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers.*;
import static org.junit.Assert.*;

public class SelectionControllerLegacyTest extends EditingTestCase {
  private TestTreeMapper rootMapper;
  private List<Selection> selectionHistory = new ArrayList<>();

  @Before
  public void setup() {
    TestTree root = new TestTree("root",
      new TestTree("a",
        new TestTree("c"),
        new TestTree("d")),
      new TestTree("b"));
    rootMapper = new TestTreeMapper(root);
    rootMapper.attachRoot();
    myCellContainer.root.children().add(rootMapper.getTarget());
    RootController.install(myCellContainer);
    SelectionController.install(myCellContainer);
    SelectionController selectionController = myCellContainer.root.get(SelectionController.PROPERTY);
    assertNotNull(selectionController);
    selectionController.addListener(new SelectionListener() {
      @Override
      public void onSelectionOpened(int selectionId, Selection selection) {
        selectionHistory.add(selection);
      }

      @Override
      public void onSelectionChanged(int selectionId, Selection selection) {
        selectionHistory.add(selection);
      }

      @Override
      public void onSelectionClosed(int selectionId) {
        selectionHistory.clear();
      }
    });
  }

  @Test
  public void empty() {
    assertTrue(selectionHistory.isEmpty());
    myCellContainer.focusedCell.set(null);
    assertTrue(selectionHistory.isEmpty());
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();
    assertTrue(selectionHistory.isEmpty());
    CellActions.toLastFocusable(rootMapper.getTarget()).run();
    assertTrue(selectionHistory.isEmpty());
  }

  @Test
  public void range() {
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertEquals(2, selectionHistory.size()); // [c, c], [c, d]
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertEquals(3, selectionHistory.size()); // [c, c], [c, d], [b, b]

    CellActions.toFirstFocusable(rootMapper.getTarget()).run();
    assertTrue(selectionHistory.isEmpty());
  }

  private static class TestCell extends VerticalCell {
    final Cell branches = horizontal();
    final TextCell labelCell = text("");
    String label;

    public TestCell() {
      to(this, labelCell, branches);
    }

    private void setLabel(String label) {
      this.label = label;
      this.labelCell.text().set(label);
    }

    @Override
    public String toString() {
      return "TestCell{" +
        "label='" + label + "'}";
    }
  }

  private static class TestTreeMapper extends Mapper<TestTree, TestCell> {
    private static MapperFactory<TestTree, Cell> FACTORY = new MapperFactory<TestTree, Cell>() {
      @Override
      public Mapper<? extends TestTree, ? extends Cell> createMapper(TestTree top) {
        return new TestTreeMapper(top);
      }
    };

    private ProjectionalRoleSynchronizer<TestTree, TestTree> sync;
    private TestTreeMapper(TestTree source) {
      super(source, new TestCell());
    }

    @Override
    protected void onBeforeAttach(MappingContext ctx) {
      getTarget().setLabel(getSource().text);
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      sync = forRole(this, getSource().branches, getTarget().branches, FACTORY);
      conf.add(sync);
    }
  }

  private static class TestTree {
    private final String text;
    private final ObservableList<TestTree> branches = new ObservableArrayList<>();

    private TestTree(String text, TestTree ... branches) {
      Collections.addAll(this.branches, branches);
      this.text = text;
    }
  }
}
