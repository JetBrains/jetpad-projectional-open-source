/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.cell.mappersUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.Arrays;
import java.util.List;

public class CounterUtil {
  public static final List<CellPropertySpec<Boolean>> PROPS = Arrays.asList(Cell.FOCUS_HIGHLIGHTED, Cell.SELECTED);

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
      for (CellPropertySpec<Boolean> cp : PROPS) {
        if (current.get(cp)) {
          update(target, cp, new PropertyChangeEvent<>(!add, add));
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
    }
    if (spec != null) {
      target.changeCounter(spec, delta);
      return true;
    }
    return false;
  }
}