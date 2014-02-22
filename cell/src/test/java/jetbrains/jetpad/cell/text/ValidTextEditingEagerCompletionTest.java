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
package jetbrains.jetpad.cell.text;

import com.google.common.base.Predicates;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionTestCase;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class ValidTextEditingEagerCompletionTest extends CompletionTestCase {
  private TextCell text = new TextCell();

  @Before
  public void init() {
    myCellContainer.root.children().add(text);

    text.text().set("");
    text.focusable().set(true);

    text.addTrait(new BaseCellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] { TextEditing.validTextEditing(Predicates.<String>alwaysTrue()) };
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createCompletion("a", "aaa", "b");
        }

        return super.get(cell, spec);
      }

      @Override
      public Object get(Cell cell, CellPropertySpec<?> spec) {
        if (spec == TextEditing.EAGER_COMPLETION) {
          return true;
        }

        return super.get(cell, spec);
      }

      @Override
      public Set<CellPropertySpec<?>> getChangedProperties(Cell cell) {
        Set<CellPropertySpec<?>> changedProperties = super.getChangedProperties(cell);
        changedProperties.add(TextEditing.EAGER_COMPLETION);
        return changedProperties;
      }
    });

    text.focus();
  }

  @Test
  public void eagerCompletionRecompletesIfThereAreMatches() {
    type("a");

    assertCompleted("a");
  }

  @Test
  public void eagerCompletionInTheMiddle() {
    text.text().set("aa");
    text.caretPosition().set(1);
    type("a");

    assertCompleted("aaa");
  }
}