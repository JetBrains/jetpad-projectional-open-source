package jetbrains.jetpad.cell;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.geometry.Vector;

public class Cells {
  public static Cell findCell(Cell current, Vector loc) {
    if (!(current instanceof IndentCell) && !current.getBounds().contains(loc)) return null;
    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;
      Cell result = findCell(child, loc);
      if (result != null) {
        return result;
      }
    }
    return current;
  }
}
