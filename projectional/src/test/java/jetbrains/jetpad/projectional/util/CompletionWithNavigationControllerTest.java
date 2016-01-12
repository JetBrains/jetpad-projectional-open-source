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
package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.completion.CompletionTestCase;
import jetbrains.jetpad.cell.indent.IndentCell;
import org.junit.Before;
import org.junit.Test;

public class CompletionWithNavigationControllerTest extends CompletionTestCase {
  private Cell target;

  @Before
  public void init() {
    target = new IndentCell();
    target.focusable().set(true);
    target.addTrait(createCompletionTrait("a", "b", "zz"));
    target.addTrait(CompletionSupport.trait());
    myCellContainer.root.children().add(target);
    CellNavigationController.install(myCellContainer);
  }

  @Test
  public void simple() {
    target.focus();

    complete();
    assertHasFrontPopup(target);

    escape();
    assertNoFrontPopup(target);
  }
}
