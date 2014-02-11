package jetbrains.jetpad.cell;

import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.view.CellContainerToViewMapper;
import jetbrains.jetpad.cell.view.MapperCell2View;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CellContainerTest {
  CellContainer container = new CellContainer();
  ViewContainer viewContainer = new ViewContainer();
  TestCell cell1 = new TestCell();
  TestCell cell2 = new TestCell();

  @Before
  public void init() {
    MapperCell2View.map(container, viewContainer);

    container.root.children().addAll(Arrays.asList(cell1, cell2));
  }

  @Test
  public void mouseEnter() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));

    assertTrue(cell1.myMouseIn);
    assertFalse(cell2.myMouseIn);
  }

  @Test
  public void mouseMove() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));
    container.mouseMoved(new MouseEvent(cell1.getBounds().center().add(new Vector(1, 1))));
    container.mouseMoved(new MouseEvent(cell2.getBounds().center()));

    assertFalse(cell1.myMouseIn);
    assertTrue(cell2.myMouseIn);
  }

  @Test
  public void mouseLeft() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));
    container.mouseLeft(new MouseEvent(new Vector(0, 0)));

    assertFalse(cell1.myMouseIn);
    assertFalse(cell2.myMouseIn);
  }

  private class TestCell extends TextCell {
    private boolean myMouseIn;

    {
      text().set("Test");

      addTrait(new BaseCellTrait() {
        @Override
        public void onMouseEntered(Cell cell, MouseEvent event) {
          super.onMouseEntered(cell, event);
          myMouseIn = true;
        }

        @Override
        public void onMouseLeft(Cell cell, MouseEvent event) {
          super.onMouseLeft(cell, event);
          myMouseIn = false;
        }
      });
    }
  }



}
