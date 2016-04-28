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
  private MappingContext mappingContext;
  private TestTree a, b, d, e, f, g, h, x, y, z;

  @Before
  public void setup() {
    TestTree root = new TestTree("root",
      a = new TestTree("a",
        new TestTree("c"),
        d = new TestTree("d"),
        e = new TestTree("e"),
        f = new TestTree("f")),
      b = new TestTree("b"),
      g = new TestTree("g",
        h = new TestTree("h")),
      x = new TestTree("x", false,
        y = new TestTree("y", false),
        z = new TestTree("z", false)));
    rootMapper = new TestTreeMapper(root);
    mappingContext = new MappingContext();
    rootMapper.attachRoot(mappingContext);
    myCellContainer.root.children().add(rootMapper.getTarget());
    RootController.install(myCellContainer);
    SelectionController.install(myCellContainer);
    SelectionController selectionController = myCellContainer.root.get(SelectionController.PROPERTY);
    assertNotNull(selectionController);
    selectionController.addListener(new SelectionListener() {
      @Override
      public void onSelectionOpened(SelectionId id, Selection selection) {
        selectionHistory.add(selection);
      }

      @Override
      public void onSelectionChanged(SelectionId id, Selection selection) {
        selectionHistory.add(selection);
      }

      @Override
      public void onSelectionClosed(SelectionId id) {
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
    get(d).labelCell.focus();
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertHistory(selection(get(d), get(d)));
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertHistory(selection(get(d), get(d)), selection(get(d), get(e)));

    get(b).labelCell.focus();
    assertTrue(selectionHistory.isEmpty());
  }

  @Test
  public void backwardRange() {
    CellActions.toEnd(get(e).labelCell).run();
    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(e), get(e)));
    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(e), get(e)), selection(get(d), get(e)));
  }

  @Test
  public void backwardMultilayer() {
    CellActions.toEnd(get(h).labelCell).run();
    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(h), get(h)));

    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(h), get(h)), selection(get(g), get(g)));

    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(h), get(h)), selection(get(g), get(g)), selection(get(b), get(g)));

    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(h), get(h)), selection(get(g), get(g)), selection(get(b), get(g)), selection(get(a), get(g)));
  }

  @Test
  public void escalation() {
    CellActions.toHome(get(f).labelCell).run();
    press(Key.RIGHT, ModifierKey.SHIFT);
    assertHistory(selection(get(f), get(f)));

    press(Key.RIGHT, ModifierKey.SHIFT);
    assertHistory(selection(get(f), get(f)), selection(get(a), get(a)), selection(get(a), get(b)));
  }

  @Test
  public void backwardEscalation() {
    CellActions.toLastFocusable(rootMapper.getTarget()).run();
    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(z), get(z)), selection(get(y), get(z)));

    press(Key.LEFT, ModifierKey.SHIFT);
    assertHistory(selection(get(z), get(z)), selection(get(y), get(z)), selection(get(x), get(x)), selection(get(g), get(x)));
  }

  private TestCell get(TestTree source) {
    return (TestCell)mappingContext.getMapper(rootMapper, source).getTarget();
  }

  private Selection selection(Cell start, Cell end) {
    return new SimpleSelection(start, null, end, null);
  }

  private void assertHistory(Selection... history) {
    assertEquals(history.length, selectionHistory.size());
    for (int i = 0; i < history.length; i++) {
      assertSame(i + " item start", history[i].getStart(), selectionHistory.get(i).getStart());
      assertSame(i + " item end", history[i].getEnd(), selectionHistory.get(i).getEnd());
    }
  }

  private static class TestCell extends VerticalCell {
    final Cell branches = horizontal();
    final TextCell labelCell = text("");
    String label;

    private TestCell(boolean focusable) {
      to(this, labelCell, branches);
      labelCell.set(FOCUSABLE, focusable);
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
      super(source, new TestCell(source.focusable));
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
    private final boolean focusable;
    private final ObservableList<TestTree> branches = new ObservableArrayList<>();

    private TestTree(String text, TestTree ... branches) {
      this(text, true, branches);
    }

    private TestTree(String text, boolean focusable, TestTree ... branches) {
      Collections.addAll(this.branches, branches);
      this.text = text;
      this.focusable = focusable;
    }
  }
}
