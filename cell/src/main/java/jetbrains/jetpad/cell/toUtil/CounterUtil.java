package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class CounterUtil {
  public static boolean isCounterProp(CellPropertySpec<?> prop) {
    return prop == Cell.FOCUS_HIGHLIGHTED || prop == Cell.SELECTED || prop == Cell.HAS_ERROR || prop == Cell.HAS_WARNING;
  }

  public static boolean update(HasCounters target, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    int delta = (Boolean) event.getNewValue() ? 1 : -1;
    CounterSpec spec = null;
    if (prop == Cell.FOCUS_HIGHLIGHTED) {
      spec = Counters.HIGHLIGHT_COUNT;
    } else if (prop == Cell.SELECTED) {
      spec = Counters.SELECT_COUNT;
    } else if (prop == Cell.HAS_ERROR) {
      spec = Counters.ERROR_COUNT;
    } else if (prop == Cell.HAS_WARNING) {
      spec = Counters.WARNING_COUNT;
    }
    if (spec != null) {
      target.changeCounter(spec, delta);
      return true;
    }
    return false;
  }
}
