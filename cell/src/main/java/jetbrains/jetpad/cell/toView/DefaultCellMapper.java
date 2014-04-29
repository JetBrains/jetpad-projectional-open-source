package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.values.Color;

class DefaultCellMapper extends BaseCellMapper<Cell, TextView> {
  DefaultCellMapper(Cell source, CellToViewContext ctx) {
    super(source, new TextView(), ctx);

    getTarget().textColor().set(Color.RED);
    getTarget().text().set("Can't create a mapper for " + source.getClass().getName());
  }
}
