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

import jetbrains.jetpad.projectional.cell.EditingTestCase;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public abstract class CompletionTestCase extends EditingTestCase {
  private String mySetTo;

  protected CompletionSupplier createCompletion(final String... items) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        for (String i : items) {
          result.add(new SetTextToCompletionItem(i));
        }
        return result;
      }
    };
  }

  protected void assertCompleted(String text) {
    assertEquals(text, mySetTo);
  }

  protected void assertNotCompleted() {
    assertNull(mySetTo);
  }

  class SetTextToCompletionItem extends SimpleCompletionItem {
    private String myText;

    SetTextToCompletionItem(String text) {
      super(text);
      myText = text;
    }

    @Override
    public CellAction complete(String text) {
      mySetTo = myText;
      return CellAction.EMPTY;
    }
  }


}