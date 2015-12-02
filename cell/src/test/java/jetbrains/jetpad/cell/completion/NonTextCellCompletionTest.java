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

import com.google.common.base.Predicates;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.completion.CompletionController;
import org.junit.Before;
import org.junit.Test;

public class NonTextCellCompletionTest extends CompletionTestCase {
  private HorizontalCell target = new HorizontalCell();
  CompletionController handler;


  @Before
  public void init() {
    target.addTrait(createCompletionTrait("a", "b", "zz"));
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
  public void asyncCompletion() {
    target.addTrait(createAsyncCompletionTrait("xxxx", "yyyy"));

    complete();
    type("xxxx");
    enter();

    assertCompleted("xxxx");
  }


  @Test
  public void completionDismissWithEscape() {
    complete();
    escape();

    assertFocused(target);
  }

  @Test
  public void completionDismissWithEscapeWithNoCurrentItem() {
    complete();
    type("zzzzz");

    escape();

    assertFocused(target);
  }


  @Test
  public void completionDismissedOnFocusLost() {
    complete();

    myCellContainer.focusedCell.set(null);

    assertNoFrontPopup(target);
  }

  @Test
  public void completionDismissOnFocusChange() {
    TextCell focusTarget = CellFactory.label("x");
    myCellContainer.root.children().add(focusTarget);

    complete();

    focusTarget.focus();

    assertNoFrontPopup(target);
  }

  @Test
  public void completionInvocationInDescendant() {
    TextCell textView = new TextCell();
    textView.addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysTrue()));
    target.children().add(textView);
    textView.focus();

    complete();

    assertHasFrontPopup(target);
  }

  @Test
  public void completionInvocationInDescendantWorksOnlyInFirstDescendant() {
    target.children().add(new TextCell());

    TextCell textView = new TextCell();
    textView.addTrait(TextEditing.validTextEditing(Predicates.<String>alwaysTrue()));
    target.children().add(textView);
    textView.focus();

    complete();

    assertNoFrontPopup(target);
  }
}