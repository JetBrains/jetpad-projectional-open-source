package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.view.CellContainerToViewMapper;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class IndentPropertiesTest extends ViewContainer {
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
    c1.highlighted().set(true);

    assertEquals(Cell.HIGHLIGHT_COLOR, getView(l11).background().get());
    assertEquals(Cell.HIGHLIGHT_COLOR, getView(l12).background().get());
    assertNull(getView(l21).background().get());
    assertNull(getView(l22).background().get());
  }

  @Test
  public void selectionUpdate() {
    c1.selected().set(true);

    assertEquals(Cell.SELECTION_COLOR, getView(l11).background().get());
    assertEquals(Cell.SELECTION_COLOR, getView(l12).background().get());
    assertNull(getView(l21).background().get());
    assertNull(getView(l22).background().get());
  }

  @Test
  public void selectionIsHigherPriorityThanHighlighting() {
    c1.selected().set(true);
    c1.highlighted().set(true);

    assertEquals(Cell.SELECTION_COLOR, getView(l11).background().get());
  }

  View getView(Cell cell) {
    Mapper<?, ?> mapper = rootMapper.getMappingContext().getMapper(rootMapper, cell);
    return (View) mapper.getTarget();
  }


}
