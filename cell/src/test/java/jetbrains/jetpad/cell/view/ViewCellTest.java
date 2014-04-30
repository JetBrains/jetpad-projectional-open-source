package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.toView.MapperCell2View;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ViewCellTest {
  private ViewContainer viewContainer = new ViewContainer();
  private CellContainer cellContainer = new CellContainer();
  private ViewCell viewCell = new ViewCell();
  private TextView testView = new TextView();

  @Before
  public void init() {
    MapperCell2View.map(cellContainer, viewContainer);
    viewCell.view.set(testView);
    cellContainer.root.children().add(viewCell);
  }

  @Test
  public void addHandling() {
    assertSame(viewContainer, testView.container());
  }

  @Test
  public void setViewToNull() {
    viewCell.view.set(null);

    assertNull(testView.container());
  }

}
