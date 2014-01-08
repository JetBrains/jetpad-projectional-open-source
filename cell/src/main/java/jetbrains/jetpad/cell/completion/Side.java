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
package jetbrains.jetpad.cell.completion;

import com.google.common.base.Function;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

import java.lang.Override;
import java.lang.String;

public enum Side {
  LEFT() {
    @Override
    public Property<Cell> getPopup(Cell cell) {
      return cell.leftPopup();
    }

    @Override
    public CellTraitPropertySpec<CompletionSupplier> getKey() {
      return Completion.LEFT_TRANSFORM;
    }

    @Override
    public Function<String, Runnable> getExpander(Cell cell) {
      return cell.get(TextEditing.EXPAND_LEFT);
    }
  },

  RIGHT() {
    @Override
    public Property<Cell> getPopup(Cell cell) {
      return cell.rightPopup();
    }

    @Override
    public CellTraitPropertySpec<CompletionSupplier> getKey() {
      return Completion.RIGHT_TRANSFORM;
    }

    @Override
    public Function<String, Runnable> getExpander(Cell cell) {
      return cell.get(TextEditing.EXPAND_RIGHT);
    }
  };

  public abstract Property<Cell> getPopup(Cell cell);
  public abstract CellTraitPropertySpec<CompletionSupplier> getKey();
  public abstract Function<String, Runnable> getExpander(Cell cell);
}