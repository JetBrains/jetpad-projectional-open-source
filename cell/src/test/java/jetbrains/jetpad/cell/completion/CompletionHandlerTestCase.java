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

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.SimpleAsync;
import jetbrains.jetpad.base.edt.TestEventDispatchThread;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.text.TextEditingTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public abstract class CompletionHandlerTestCase extends CompletionTestCase {
  protected abstract Cell getView();
  protected abstract ScrollCell getCompletionMenu();

  protected CompletionController getController() {
    return getView().get(Completion.COMPLETION_CONTROLLER);
  }

  @Test
  public void completionHandlerAvailable() {
    assertNotNull(getController());
    assertFalse(getController().isActive());
  }

  @Test
  public void completionHandlerReturnCurrentValueAfterComplete() {
    complete();

    assertTrue(getController().isActive());
  }

  @Test
  public void completionCanBeActivatedWithCompletionHandler() {
    getController().activate();

    assertTrue(getController().isActive());
  }

  @Test
  public void completionCanBeDeactivatedWithCompletionHandler() {
    assertSame(getView(), myCellContainer.focusedCell.get());
    complete();

    getController().deactivate();
    assertFalse(getController().isActive());
    assertSame(getView(), myCellContainer.focusedCell.get());
  }

  @Test
  public void completeTwiceDefaultEditor() {
    complete();
    complete();
    assertTrue(getController().isActive());
  }

  @Test
  public void completeTwiceCustomEditor() {
    addCustomEditor();

    complete();
    assertTrue(getController().isActive());
    complete();
    assertTrue(getController().isActive());
  }

  @Test
  public void completionDeactivateWithCustomEditor() {
    getView().focusable().set(false);
    addCustomEditor();

    complete();
    assertTrue(getController().isActive());

    escape();
    assertFalse(getController().isActive());
  }

  private TextCell addCustomEditor() {
    TextCell textCell = new TextCell();
    textCell.addTrait(new TextEditingTrait());
    textCell.focusable().set(true);
    getView().children().add(textCell);
    getView().set(CompletionSupport.EDITOR, textCell);
    textCell.focus();
    return textCell;
  }

  @Test
  public void loadingCompletionPlaceholderShownAfterDelay() {
    final TestEventDispatchThread edt = new TestEventDispatchThread();
    CellContainerEdtUtil.resetEdt(myCellContainer, edt);

    getView().addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public Async<Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
              final SimpleAsync<Iterable<CompletionItem>> completionAsync = new SimpleAsync<>();
              edt.schedule(CompletionMenu.EMPTY_COMPLETION_DELAY + 1, new Runnable() {
                @Override
                public void run() {
                  completionAsync.success(createItems("a", "b"));
                }
              });
              return completionAsync;
            }
          };
        }
        return super.get(cell, spec);
      }
    });

    complete();
    assertCompletionMenuState(false, false, null);

    edt.executeUpdates(CompletionMenu.EMPTY_COMPLETION_DELAY);
    assertCompletionMenuState(false, true, "Loading");

    edt.executeUpdates(1);
    assertCompletionMenuState(true, false, null);
  }

  protected final void assertCompletionMenuState(boolean hasCompletionItems, boolean placeholderVisible,
      String placeholderText) {
    assertTrue(getController().isActive());

    ScrollCell menu = getCompletionMenu();
    VerticalCell content = (VerticalCell) menu.children().get(0);
    assertEquals(2, content.children().size());
    VerticalCell items = (VerticalCell) content.children().get(0);
    assertEquals(hasCompletionItems, !items.children().isEmpty());
    TextCell placeholder = (TextCell) content.children().get(1);
    assertEquals(placeholderVisible, placeholder.visible().get());
    if (placeholderVisible) {
      assertTrue(placeholder.text().get().contains(placeholderText));
    }
  }
}