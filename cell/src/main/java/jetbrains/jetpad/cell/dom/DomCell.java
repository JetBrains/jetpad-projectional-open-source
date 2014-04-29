package jetbrains.jetpad.cell.dom;

import com.google.gwt.dom.client.Node;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.Property;

public class DomCell extends Cell {
  public static final CellPropertySpec<Node> NODE = new CellPropertySpec<>("node");

  public final Property<Node> node = getProp(NODE);
}
