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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.completion.CompletionController;

public abstract class BaseCompletionController implements CompletionController {
  private static final CellPropertySpec<Boolean> ACTIVE = new CellPropertySpec<>("isCompletionActive", false);

  public static boolean isCompletionActive(Cell cell) {
    return cell.get(ACTIVE);
  }

  private Cell myCell;

  protected BaseCompletionController(Cell cell) {
    myCell = cell;
  }

  @Override
  public final boolean isActive() {
    return isCompletionActive(myCell);
  }

  @Override
  public final void activate() {
    activate(Runnables.EMPTY);
  }

  @Override
  public final void activate(Runnable restoreState) {
    if (isActive()) {
      throw new IllegalStateException();
    }
    myCell.set(ACTIVE, true);
    doActivate(Runnables.seq(new Runnable() {
      @Override
      public void run() {
        myCell.set(ACTIVE, false);
      }
    }, restoreState));
  }

  @Override
  public final void deactivate() {
    if (!isActive()) {
      throw new IllegalStateException();
    }
    myCell.set(ACTIVE, false);
    doDeactivate();
  }

  protected abstract void doActivate(Runnable restoreState);
  protected abstract void doDeactivate();
}
