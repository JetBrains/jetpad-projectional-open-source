package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.Arrays;
import java.util.List;

public class CounterUtil {
  public static final List<CellPropertySpec<Boolean>> PROPS = Arrays.asList(Cell.FOCUS_HIGHLIGHTED, Cell.SELECTED, Cell.HAS_ERROR, Cell.HAS_WARNING);

  public static boolean isCounterProp(CellPropertySpec<?> prop) {
    return PROPS.indexOf(prop) != -1;
  }

  public static void updateOnAdd(Cell root, Cell cell, HasCounters target) {
    update(true, root, cell, target);
  }

  public static void updateOnRemove(Cell root, Cell cell, HasCounters target) {
    update(false, root, cell, target);
  }

  private static void update(boolean add, Cell root, Cell cell, HasCounters target) {
    Cell current = cell;
    do {
      current = current.getParent();
      for (CellPropertySpec<Boolean> cp : CounterUtil.PROPS) {
        if (current.get(cp)) {
          CounterUtil.update(target, cp, new PropertyChangeEvent<>(!add, add));
        }
      }
    } while (current != root);
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
