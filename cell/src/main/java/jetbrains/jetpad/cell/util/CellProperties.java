package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.Selector;

public class CellProperties {
  public static ReadableProperty<Boolean> focusIn(final Cell cell) {
    final ReadableProperty<Cell> focusedCell = Properties.select(cell.cellContainer(), new Selector<CellContainer, ReadableProperty<Cell>>() {
      @Override
      public ReadableProperty<Cell> select(CellContainer source) {
        return source.focusedCell;
      }
    });

    return new DerivedProperty<Boolean>(focusedCell) {
      @Override
      public Boolean get() {
        Cell focused = focusedCell.get();
        if (focused == null) return false;
        return Composites.isDescendant(cell, focused);
      }
    };
  }
}
