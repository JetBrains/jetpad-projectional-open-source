package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class CellToViewTest {
  private ViewContainer targetViewContainer = new ViewContainer();
  private CellView cellView = new CellView();

  @Before
  public void init() {
    targetViewContainer.contentRoot().children().add(cellView);
  }

  @Test
  public void cellContainerRemovalInHandlers() {
    TextCell testCell = new TextCell();
    testCell.addTrait(new BaseCellTrait() {
      @Override
      public void onMousePressed(Cell cell, MouseEvent event) {
        targetViewContainer.contentRoot().children().clear();
        event.consume();
      }
    });
    cellView.cell.set(testCell);

    targetViewContainer.root().validate();
    targetViewContainer.mousePressed(new MouseEvent(testCell.getBounds().center()));
    
    assertFalse(cellView.isAttached());
  }



}
