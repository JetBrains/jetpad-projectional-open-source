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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Validators;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.text.TextEditor;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.CellFactory;

public class ProjectionalSynchronizerPlaceholders {
  private static final String DEFAULT_TEXT = "<empty>";

  public static Supplier<PlaceholderCell> empty() {
    return text(null);
  }

  public static Supplier<PlaceholderCell> text(final String text) {
    return new Supplier<PlaceholderCell>() {
      @Override
      public PlaceholderCell get() {
        return new HeterogeneousPlaceholder(text);
      }
    };
  }

  static class HeterogeneousPlaceholder implements PlaceholderCell {
    private TextCell myTextCell;
    private String myPlaceholderText;
    private Cell myContainer;

    HeterogeneousPlaceholder(String placeholderText) {
      myPlaceholderText = placeholderText == null ? DEFAULT_TEXT : placeholderText;
      myTextCell = new TextCell();
      myTextCell.addTrait(TextEditing.validTextEditing(Validators.equalsTo("")));
    }

    @Override
    public Cell getCell() {
      if (myContainer == null) {
        throw new IllegalStateException();
      }
      return myContainer;
    }

    @Override
    public TextEditor getEditor() {
      return TextEditing.textEditor(myTextCell);
    }

    void setup(boolean indent, CellTrait completionTrait) {
      myTextCell.addTrait(completionTrait);
      //todo this is tmp hack
      if (indent) {
        myContainer = CellFactory.indent(myTextCell, CellFactory.placeHolder(myTextCell, myPlaceholderText));
      } else {
        myContainer = CellFactory.horizontal(myTextCell, CellFactory.placeHolder(myTextCell, myPlaceholderText));
      }
      myContainer.addTrait(new CellTrait() {
        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == PlaceholderCell.FOCUSABLE_ITEM) {
            return myTextCell;
          }
          return super.get(cell, spec);
        }
      });
    }
  }
}
