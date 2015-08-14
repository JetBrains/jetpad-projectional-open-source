package jetbrains.jetpad.cell.util;

public interface CellState {
  CellStateDifference getDifference(CellState state);
}
