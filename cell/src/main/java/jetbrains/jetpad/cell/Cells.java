package jetbrains.jetpad.cell;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.geometry.Vector;

public class Cells {
  public static Cell findCell(Cell current, Vector loc) {
    boolean isIndent = current instanceof IndentCell;
    if (!isIndent && !current.getBounds().contains(loc)) return null;
    if (current instanceof NewLineCell) return null;

    for (Cell child : current.children()) {
      if (!child.visible().get()) continue;
      Cell result = findCell(child, loc);
      if (result != null) {
        return result;
      }
    }

    if (!isIndent && current.getBounds().contains(loc)) {
      return current;
    } else {
      return null;
    }
  }
}
