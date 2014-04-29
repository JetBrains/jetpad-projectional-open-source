package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.View;

public class ViewCell extends Cell {
  public static final CellPropertySpec<View> VIEW = new CellPropertySpec<>("view");

  public final Property<View> view = getProp(VIEW);


}
