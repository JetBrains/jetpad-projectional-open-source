package jetbrains.jetpad.cell.util;

public interface CellState {
  CellStateDifference compareTo(CellState state);
}
