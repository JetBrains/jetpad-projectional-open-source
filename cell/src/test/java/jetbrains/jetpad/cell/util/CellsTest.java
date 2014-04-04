package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CellsTest {
  @Test
  public void emptinessOfEmptyText() {
    TextCell cell = new TextCell();
    cell.text().set("");

    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void emptinessOfNullText() {
    TextCell cell = new TextCell();
    cell.text().set(null);

    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void nonEmptinessOfNonEmptyText() {
    TextCell cell = new TextCell();
    cell.text().set("aaa");

    assertFalse(Cells.isEmpty(cell));
  }

  @Test
  public void nonEmptinessOfComposite() {
    assertFalse(Cells.isEmpty(new HorizontalCell()));
  }

  @Test
  public void emptinessOfCellWithPositionHandler() {
    VerticalCell cell = new VerticalCell();
    cell.children().add(new TextCell());
    cell.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == PositionHandler.PROPERTY) {
          return new PositionHandler() {
            @Override
            public boolean isHome() {
              return true;
            }

            @Override
            public boolean isEnd() {
              return true;
            }

            @Override
            public void home() {
            }

            @Override
            public void end() {
            }

            @Override
            public Property<Integer> caretOffset() {
              return new ValueProperty<>(0);
            }
          };
        }

        return super.get(cell, spec);
      }
    });

    assertTrue(Cells.isEmpty(cell));
  }
}
