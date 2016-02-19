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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.completion.BaseCompletionParameters;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionParameters;

public final class CellCompletionController implements CompletionController {
  private static final CellPropertySpec<Boolean> ACTIVE = new CellPropertySpec<>("isCompletionActive", false);

  public static boolean isCompletionActive(Cell cell) {
    return cell.get(ACTIVE);
  }

  private Cell myCell;
  private boolean myMenu;

  public CellCompletionController(Cell cell) {
    this(cell, false);
  }

  public CellCompletionController(Cell cell, boolean isMenu) {
    myCell = cell;
    myMenu = isMenu;
  }

  @Override
  public boolean isActive() {
    return isCompletionActive(myCell);
  }

  @Override
  public boolean canActivate() {
    return !Completion.isCompletionEmpty(myCell, myMenu ? menuCompletionParameters() : CompletionParameters.EMPTY);
  }

  @Override
  public void activate() {
    activate(Runnables.EMPTY);
  }

  // NB: note the actual purpose of Runnable passed to this method!
  @Override
  public void activate(Runnable restoreFocus) {
    if (isActive()) {
      throw new IllegalStateException();
    }
    myCell.set(ACTIVE, true);
    Runnable state = myCell.getContainer().saveState();
    Runnable deactivate = CellCompletionController.this::doDeactivate;
    CompletionSupport.showCompletion(
        myCell.get(CompletionSupport.EDITOR),
        Completion.allCompletion(myCell, menuCompletionParameters()),
        deactivate,
        Runnables.seq(state, restoreFocus));
  }

  @Override
  public void deactivate() {
    if (!isActive()) {
      throw new IllegalStateException();
    }
    doDeactivate();
    myCell.focus();
  }

  private void doDeactivate() {
    if (isActive()) {
      myCell.set(ACTIVE, false);
      myCell.get(CompletionSupport.EDITOR).disable();
    }
  }

  @Override
  public boolean hasAmbiguousMatches() {
    if (!TextEditing.isTextEditor(myCell)) {
      return true;
    }
    CompletionItems helper = Completion.completionFor(myCell, menuCompletionParameters());
    return !helper.hasSingleMatch(TextEditing.getPrefixText(TextEditing.textEditor(myCell)), false);
  }

  private CompletionParameters menuCompletionParameters() {
    return new BaseCompletionParameters() {
      @Override
      public boolean isMenu() {
        return true;
      }
    };
  }
}