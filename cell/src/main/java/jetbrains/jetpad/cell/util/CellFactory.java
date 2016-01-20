/*
 * Copyright 2012-2015 JetBrains s.r.o
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
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.text.TextEditor;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.Arrays;
import java.util.Collections;

public class CellFactory {
  public static Cell to(Cell target, Cell... cells) {
    Collections.addAll(target.children(), cells);
    return target;
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
    result.indented().set(indent);
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
    TextCell result = new TextCell(text);

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

  public static TextCell text(String text) {
    return new TextCell(text);
  }

  public static TextCell keyword(final String text) {
    TextCell result = new TextCell();
    result.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return TextEditing.textNavigation(true, true);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(TextCell.TEXT_COLOR, Color.DARK_BLUE);
        collector.add(TextCell.TEXT, text);
        collector.add(TextCell.BOLD, true);
        super.provideProperties(cell, collector);
      }
    });
    return result;
  }

  public static Cell space() {
    return space(" ");
  }

  public static Cell space(String text) {
    return new TextCell(text);
  }

  public static TextCell placeHolder(TextCell textCell, String text) {
    return placeHolder(TextEditing.textEditor(textCell), text);
  }

  public static TextCell placeHolder(final TextEditor editor, String text) {
    final TextCell placeholder = new TextCell(text);
    placeholder.set(TextCell.TEXT_COLOR, Color.GRAY);
    placeholder.visible().set(Strings.isNullOrEmpty(editor.text().get()));
    placeholder.addTrait(new CellTrait() {
      @Override
      public void onMousePressed(Cell cell, MouseEvent event) {
        if (!editor.focusable().get()) return;
        editor.focus();
        event.consume();
      }
    });
    editor.text().addHandler(new EventHandler<PropertyChangeEvent<String>>() {
      @Override
      public void onEvent(PropertyChangeEvent<String> event) {
        placeholder.visible().set(Strings.isNullOrEmpty(event.getNewValue()));
      }
    });
    editor.focusHighlighted().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        placeholder.focusHighlighted().set(event.getNewValue());
      }
    });
    return placeholder;
  }
}