/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell.support;

import com.google.common.base.Function;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.completion.Completion;
import jetbrains.jetpad.projectional.cell.completion.CompletionSupplier;
import jetbrains.jetpad.projectional.cell.text.TextEditing;
import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.cell.action.CellAction;

import java.lang.Override;
import java.lang.String;

public enum Side {
  LEFT() {
    @Override
    Property<Cell> getPopup(Cell cell) {
      return cell.leftPopup();
    }

    @Override
    CellTraitPropertySpec<CompletionSupplier> getKey() {
      return Completion.LEFT_TRANSFORM;
    }

    @Override
    Function<String, CellAction> getExpander(Cell cell) {
      return cell.get(TextEditing.EXPAND_LEFT);
    }
  },

  RIGHT() {
    @Override
    Property<Cell> getPopup(Cell cell) {
      return cell.rightPopup();
    }

    @Override
    CellTraitPropertySpec<CompletionSupplier> getKey() {
      return Completion.RIGHT_TRANSFORM;
    }

    @Override
    Function<String, CellAction> getExpander(Cell cell) {
      return cell.get(TextEditing.EXPAND_RIGHT);
    }
  };

  abstract Property<Cell> getPopup(Cell cell);


  abstract CellTraitPropertySpec<CompletionSupplier> getKey();
  abstract Function<String, CellAction> getExpander(Cell cell);
}