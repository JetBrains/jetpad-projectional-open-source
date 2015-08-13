package jetbrains.jetpad.cell.util;

public enum CellStateDifference {
  EQUAL, NAVIGATION, EDIT;

  public static CellStateDifference max(CellStateDifference difference1, CellStateDifference difference2) {
    return difference1.ordinal() > difference2.ordinal() ? difference1 : difference2;
  }
}
