/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.cell.util;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.base.animation.AnimatedList;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

import java.util.List;

public class CellLists {
  public static final CellTraitPropertySpec<Boolean> NO_SPACE_TO_LEFT = new CellTraitPropertySpec<>("noSpaceToLeft", false);
  public static final CellTraitPropertySpec<Boolean> NO_SPACE_TO_RIGHT = new CellTraitPropertySpec<>("noSpaceToRight", false);

  public static List<Cell> separated(List<Cell> list, final String separator) {
    return new SeparatedCellList(list) {
      @Override
      protected TextCell createSeparator(Cell left, Cell right) {
        return new TextCell(separator);
      }
    };
  }

  public static List<Cell> separated(List<Cell> list, final Supplier<Cell> supplier) {
    return new SeparatedCellList(list) {
      @Override
      protected Cell createSeparator(Cell left, Cell right) {
        return supplier.get();
      }
    };
  }

  public static List<Cell> newLineSeparated(List<Cell> list) {
    return new SeparatedCellList(list) {
      @Override
      protected Cell createSeparator(Cell left, Cell right) {
        return CellFactory.newLine();
      }
    };
  }

  public static List<Cell> spaced(List<Cell> list) {
    return new SeparatedCellList(list) {
      @Override
      protected Cell createSeparator(Cell left, Cell right) {
        if (left.get(NO_SPACE_TO_RIGHT) || right.get(NO_SPACE_TO_LEFT)) {
          return new HorizontalCell();
        } else {
          return new TextCell(" ");
        }
      }
    };
  }

  public static List<Cell> animated(final List<Cell> baseList, final Function<Cell, Animation> add, final Function<Cell, Animation> remove) {
    return new AnimatedList<Cell>(baseList) {
      @Override
      public Animation addAnimation(Cell e) {
        return add.apply(e);
      }

      @Override
      public Animation removeAnimation(Cell e) {
        return remove.apply(e);
      }
    };
  }

  public static List<Cell> animated(List<Cell> baseList) {
    return animated(baseList, new Function<Cell, Animation>() {
      @Override
      public Animation apply(Cell input) {
        return input.fadeIn(300);
      }
    }, new Function<Cell, Animation>() {
      @Override
      public Animation apply(Cell input) {
        return input.fadeOut(300);
      }
    });
  }

  public static List<Cell> animated(Cell cell) {
    return animated(cell.children());
  }
}