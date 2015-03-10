package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.values.Color;

public class AncestorUtil {
  public static Color getAncestorBackground(Cell container, Cell leaf) {
    Cell current = leaf;
    do {
      current = current.getParent();
      if (current.background().get() != null) {
        return current.background().get();
      }
    } while (current != container);
    return null;
  }

}
