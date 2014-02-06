package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static jetbrains.jetpad.cell.util.CellFactory.*;
import static org.junit.Assert.*;

public class WithPropertyIndexTest {
  private CellContainer container;
  private TextCell c = label("c");
  private TextCell b = label("b");
  private TextCell a = label("a");
  private CellPropertySpec<String> TEST_PROP = new CellPropertySpec<String>("testProp");
  private WithPropertyIndex index;

  @Before
  public void init() {
    container = new CellContainer();
    container.root.children().add(horizontal(a, b, c));
  }

  @Test
  public void initialization() {
    setProp(a, b);

    initIndex();

    assertIndex(a, b);
  }

  @Test
  public void setProperty() {
    initIndex();

    setProp(a, c);

    assertIndex(a, c);
  }

  @Test
  public void unsetProperty() {
    initIndex();

    setProp(a);
    unsetProp(a);

    assertIndex();
  }

  @Test
  public void removeWithProp() {
    initIndex();

    setProp(a);

    container.root.children().clear();

    assertIndex();
  }

  @Test
  public void addWithProp() {
    initIndex();

    Cell c1 = label("c1");
    Cell c2 = label("c2");

    setProp(c1, c2);

    a.children().add(horizontal(c1, c2));


    assertIndex(c1, c2);
  }


  @Test
  public void dispose() {
    initIndex();

    setProp(a, b);

    disposeIndex();

    assertIndex();
  }


  private void initIndex() {
    index = new WithPropertyIndex(container, TEST_PROP);
  }

  private void disposeIndex() {
    index.dispose();
  }

  private void setProp(Cell... cells) {
    for (Cell cell : cells) {
      cell.set(TEST_PROP, "+");
    }
  }

  private void unsetProp(Cell... cells) {
    for (Cell cell : cells) {
      cell.set(TEST_PROP, null);
    }
  }

  private void assertIndex(Cell... cells) {
    assertEquals(new HashSet<Cell>(Arrays.asList(cells)), index.withProperty());
  }


}
