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

import com.google.common.base.Predicates;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.completion.CompletionController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NonTextCellCompletionTest extends CompletionTestCase {
  private HorizontalCell target = new HorizontalCell();
  CompletionController handler;


  @Before
  public void init() {
    target.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createCompletion("a", "b", "zz");
        }
        return super.get(cell, spec);
      }
    });
    target.addTrait(CompletionSupport.trait());

    target.focusable().set(true);
    myCellContainer.root.children().add(target);

    target.focus();

    handler = target.get(Completion.COMPLETION_CONTROLLER);
  }

  @Test
  public void simpleCompletion() {
    complete();
    enter();

    assertCompleted("a");
  }

  @Test
  public void completionDismissWithEscape() {
    complete();
    escape();

    assertFocused(target);
  }

  @Test
  public void completionDismissedOnFocusLost() {
    complete();

    myCellContainer.focusedCell.set(null);

    assertNull(target.frontPopup().get());
  }

  @Test
  public void completionDismissOnFocusChange() {
    TextCell focusTarget = CellFactory.label("x");
    myCellContainer.root.children().add(focusTarget);

    complete();

    focusTarget.focus();

    assertNull(target.frontPopup().get());
  }

  @Test
  public void completionInvocationInDescendant() {
    TextCell textView = new TextCell();
    textView.addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysTrue()));
    target.children().add(textView);
    textView.focus();

    complete();

    assertNotNull(target.frontPopup().get());
  }

  @Test
  public void completionInvocationInDescendantWorksOnlyInFirstDescendant() {
    target.children().add(new TextCell());

    TextCell textView = new TextCell();
    textView.addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysTrue()));
    target.children().add(textView);
    textView.focus();

    complete();

    assertNull(target.frontPopup().get());
  }
}