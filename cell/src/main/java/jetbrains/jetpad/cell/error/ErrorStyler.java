package jetbrains.jetpad.cell.error;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.values.Color;

public class ErrorStyler {
  public static final Color WARNING_COLOR = new Color(244, 232, 171);

  protected Registration doApplyBroken(Cell cell) {
    return cell.set(Cell.BACKGROUND, Color.LIGHT_PINK);
  }

  protected Registration doApplyError(Cell cell) {
    return cell.set(Cell.BORDER_COLOR, Color.PINK);
  }

  protected Registration doApplyWarning(Cell cell) {
    return cell.set(Cell.BACKGROUND, WARNING_COLOR);
  }
}
