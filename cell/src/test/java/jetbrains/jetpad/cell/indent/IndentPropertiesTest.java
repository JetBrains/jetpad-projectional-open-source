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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.toView.CellContainerToViewMapper;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class IndentPropertiesTest {
  private ViewContainer viewContainer = new ViewContainer();
  private CellContainer cellContainer = new CellContainer();
  private IndentCell indentCell = new IndentCell();
  private IndentCell c1 = new IndentCell();
  private IndentCell c2 = new IndentCell();
  private TextCell l11 = new TextCell("l11");
  private TextCell l12 = new TextCell("l12");
  private TextCell l21 = new TextCell("l21");
  private TextCell l22 = new TextCell("l22");
  private CellContainerToViewMapper rootMapper;

  @Before
  public void init() {
    cellContainer.root.children().add(indentCell);
    indentCell.children().addAll(Arrays.asList(c1, c2));
    c1.children().addAll(Arrays.asList(l11, l12));
    c2.children().addAll(Arrays.asList(l21, l22));


    viewContainer.contentRoot().focusable().set(true);
    viewContainer.focusedView().set(viewContainer.contentRoot());
    rootMapper = new CellContainerToViewMapper(cellContainer, viewContainer.root(), viewContainer.contentRoot(), viewContainer.decorationRoot());
    rootMapper.attachRoot();
  }

  @Test
  public void highlightingUpdate() {
    c1.focusHighlighted().set(true);

    assertHighlighted(l11, l12);
    assertBlank(l21, l22);
  }

  @Test
  public void overlappingHighlightUpdate() {
    indentCell.focusHighlighted().set(true);
    c1.focusHighlighted().set(true);
    indentCell.focusHighlighted().set(false);
  }

  @Test
  public void addChildToHighlightePlace() {
    c1.focusHighlighted().set(true);

    TextCell l13 = new TextCell("l13");
    c1.children().add(l13);

    assertHighlighted(l13);
  }

  @Test
  public void selectionUpdate() {
    c1.selected().set(true);

    assertSelected(l11, l12);
    assertBlank(l21, l22);
  }

  @Test
  public void selectionIsHigherPriorityThanHighlighting() {
    c1.selected().set(true);
    c1.focusHighlighted().set(true);

    assertSelected(l11);
  }

  @Test
  public void emptyIndentBounds() {
    c1.children().clear();
    assertNotNull(Cells.indentBounds(c1));
  }

  View getView(Cell cell) {
    Mapper<?, ?> mapper = rootMapper.getMappingContext().getMapper(rootMapper, cell);
    return (View) mapper.getTarget();
  }


  private void assertSelected(Cell... cs) {
    for (Cell c : cs) {
      assertEquals(CellContainerToViewMapper.SELECTION_COLOR, getView(c).background().get());
    }
  }

  private void assertHighlighted(Cell... cs) {
    for (Cell c : cs) {
      assertEquals(CellContainerToViewMapper.FOCUS_HIGHLIGHT_COLOR, getView(c).background().get());
    }
  }

  private void assertBlank(Cell... cs) {
    for (Cell c : cs) {
      assertNull(getView(c).background().get());
    }
  }

}