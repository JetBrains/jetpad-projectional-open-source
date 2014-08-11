package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.cell.Cell;

public class MatchingConstructs {
  public static void pair(Cell c1, Cell c2) {
    pairOneWay(c1, c2);
    pairOneWay(c2, c1);
  }

  private static void pairOneWay(Cell c1, Cell c2) {
    c1.set(CellNavigationController.PAIR_CELL, c2);
  }
}
