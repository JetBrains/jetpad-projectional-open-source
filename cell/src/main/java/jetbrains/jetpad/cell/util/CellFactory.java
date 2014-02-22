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

import com.google.common.base.Strings;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.values.Color;

import java.util.Arrays;
import java.util.Collections;

public class CellFactory {
  private static final CellTraitPropertySpec<Cell> PLACEHOLDER_CELL = new CellTraitPropertySpec<>("placeholderCell");

  public static void to(Cell target, Cell... cells) {
    Collections.addAll(target.children(), cells);
  }

  public static HorizontalCell horizontal(Cell... cells) {
    HorizontalCell result = new HorizontalCell();
    to(result, cells);
    return result;
  }

  public static VerticalCell vertical(Cell... cells) {
    return vertical(false, cells);
  }

  public static VerticalCell vertical(boolean indent, Cell... cells) {
    VerticalCell result = new VerticalCell();
    result.indent().set(indent);
    to(result, cells);
    return result;
  }

  public static IndentCell indent(Cell... cells) {
    return indent(false, cells);
  }

  public static IndentCell indent(boolean indent, Cell... cells) {
    IndentCell result = new IndentCell(indent);
    result.children().addAll(Arrays.asList(cells));
    return result;
  }

  public static NewLineCell newLine() {
    return new NewLineCell();
  }

  public static TextCell label(String text, boolean firstAllowed, boolean lastAllowed) {
    TextCell result = new TextCell();
    result.text().set(text);

    boolean focusable;
    if (text.length() == 0) {
      focusable = firstAllowed && lastAllowed;
    } else if (text.length() == 1) {
      focusable = firstAllowed || lastAllowed;
    } else {
      focusable = true;
    }

    if (focusable) {
      result.addTrait(TextEditing.textNavigation(firstAllowed, lastAllowed));
    }
    return result;
  }

  public static TextCell label(String text) {
    return label(text, true, true);
  }

  public static TextCell keyword(final String text) {
    TextCell result = new TextCell();
    result.addTrait(new BaseCellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] { TextEditing.textNavigation(true, true) };
      }

      @Override
      public Object get(Cell cell, CellPropertySpec<?> spec) {
        if (spec == TextCell.TEXT_COLOR) {
          return Color.DARK_BLUE;
        }

        if (spec == TextCell.TEXT) {
          return text;
        }

        if (spec == TextCell.BOLD) {
          return true;
        }

        return super.get(cell, spec);
      }
    });
    return result;
  }

  public static Cell space() {
    return space(" ");
  }

  public static Cell space(String text) {
    TextCell result = new TextCell();
    result.text().set(text);
    return result;
  }

  public static TextCell placeHolder(final TextCell textCell, String text) {
    final TextCell result = new TextCell();
    result.text().set(text);

    result.addTrait(new BaseCellTrait() {

      @Override
      public void onMousePressed(Cell cell, MouseEvent event) {
        textCell.focus();
        event.consume();
      }
    });
    textCell.addTrait(new BaseCellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == PLACEHOLDER_CELL) {
          return result;
        }

        return super.get(cell, spec);
      }

      @Override
      public void onPropertyChanged(Cell c, CellPropertySpec<?> prop, PropertyChangeEvent<?> e) {
        if (prop == Cell.SELECTED) {
          PropertyChangeEvent<Boolean> event = (PropertyChangeEvent<Boolean>) e;
          textCell.get(PLACEHOLDER_CELL).selected().set(event.getNewValue());
        }

        if (prop == TextCell.TEXT) {
          PropertyChangeEvent<String> event = (PropertyChangeEvent<String>) e;
          textCell.get(PLACEHOLDER_CELL).visible().set(Strings.isNullOrEmpty(event.getNewValue()));
        }

        super.onPropertyChanged(c, prop, e);
      }
    });
    result.set(TextCell.TEXT_COLOR, Color.GRAY);
    result.visible().set(Strings.isNullOrEmpty(textCell.text().get()));
    return result;
  }
}