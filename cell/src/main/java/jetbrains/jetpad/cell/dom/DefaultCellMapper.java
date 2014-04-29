package jetbrains.jetpad.cell.dom;

import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.Cell;

class DefaultCellMapper extends BaseCellMapper<Cell> {
  DefaultCellMapper(Cell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());

    getTarget().getStyle().setColor("red");
    getTarget().setInnerText("Can't create a mapper for " + source.getClass().getName());
  }
}
