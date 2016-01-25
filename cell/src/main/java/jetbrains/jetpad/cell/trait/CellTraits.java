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
package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.model.composite.Composites;

public class CellTraits {
  public static Registration captureTo(Cell root, final CellTrait targetTrait) {
    final Value<Registration> targetReg = new Value<>(Registration.EMPTY);

    final CellContainer container = root.getContainer();
    if (container != null && container.focusedCell.get() != null && Composites.isDescendant(root, container.focusedCell.get())) {
      targetReg.set(container.focusedCell.get().addTrait(targetTrait));
    }

    final Registration rootReg = root.addTrait(new CellTrait() {
      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        super.onFocusGained(cell, event);
        targetReg.get().remove();
        targetReg.set(event.getNewValue().addTrait(targetTrait));
      }

      @Override
      public void onFocusLost(Cell cell, FocusEvent event) {
        targetReg.get().remove();
        targetReg.set(Registration.EMPTY);
        super.onFocusLost(cell, event);
      }
    });

    return new Registration() {
      @Override
      protected void doRemove() {
        targetReg.get().remove();
        rootReg.remove();
      }
    };
  }
}