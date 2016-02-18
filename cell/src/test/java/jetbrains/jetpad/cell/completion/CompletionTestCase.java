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
import jetbrains.jetpad.base.Asyncs;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class CompletionTestCase extends EditingTestCase {
  private String mySetTo;

  protected CompletionSupplier createCompletion(final String... items) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        return createItems(items);
      }
    };
  }

  protected CellTrait createCompletionTrait(final String... items) {
    return new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createCompletion(items);
        }
        return super.get(cell, spec);
      }
    };
  }

  protected CompletionSupplier createAsyncCompletion(final String... items) {
    return new CompletionSupplier() {
      @Override
      public Async<Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
        return Asyncs.constant(createItems(items));
      }
    };
  }

  protected CellTrait createAsyncCompletionTrait(final String... items) {
    return new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createAsyncCompletion(items);
        }

        return super.get(cell, spec);
      }
    };
  }

  private List<CompletionItem> createItems(String... items) {
    List<CompletionItem> result = new ArrayList<>();
    for (String i : items) {
      result.add(new SetTextToCompletionItem(i));
    }
    return result;
  }

  protected void assertCompleted(String text) {
    assertEquals(text, mySetTo);
  }

  protected void assertNotCompleted() {
    assertNull(mySetTo);
  }

  protected class SetTextToCompletionItem extends SimpleCompletionItem {
    protected String myCompletion;

    public SetTextToCompletionItem(String text) {
      this(text, text);
    }

    public SetTextToCompletionItem(String match, String completion) {
      super(match);
      myCompletion = completion;
    }

    @Override
    public Runnable complete(String text) {
      mySetTo = myCompletion;
      return Runnables.EMPTY;
    }
  }


}