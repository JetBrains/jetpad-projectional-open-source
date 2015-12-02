package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.values.Color;

public class AncestorUtil {
  public static Color getAncestorBackground(Cell container, Cell leaf) {
    Cell current = leaf;
    do {
      current = current.getParent();
      Color bg = current.get(Cell.BACKGROUND);
      if (bg != null) {
        return bg;
      }
    } while (current != container);
    return null;
  }
}
