package jetbrains.jetpad.cell;

import jetbrains.jetpad.model.composite.Composites;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

public class CellNavCompositeTest {
  private Cell container = new HorizontalCell();
  private Cell c1 = new TextCell();
  private Cell c2 = new TextCell();
  private Cell c3 = new TextCell();


  @Test
  public void addChildren() {
    container.children().addAll(Arrays.asList(c1, c2, c3));

    checkInvariants();
  }


  @Test
  public void removeMiddleChild() {
    container.children().addAll(Arrays.asList(c1, c2, c3));
    container.children().remove(c2);

    checkInvariants();
  }

  @Test
  public void removeFirstChild() {
    container.children().addAll(Arrays.asList(c1, c2, c3));
    container.children().remove(c1);

    checkInvariants();
  }

  @Test
  public void removeLastChild() {
    container.children().addAll(Arrays.asList(c1, c2, c3));
    container.children().remove(c3);

    checkInvariants();
  }


  @Test
  public void setChild() {
    container.children().addAll(Arrays.asList(c1, c2));
    container.children().set(1, c3);

    checkInvariants();
  }


  private void checkInvariants() {
    for (Cell c : container.children()) {
      assertSame(c.prevSibling(), Composites.prevSibling(c));
      assertSame(c.nextSibling(), Composites.nextSibling(c));
    }
  }

}
