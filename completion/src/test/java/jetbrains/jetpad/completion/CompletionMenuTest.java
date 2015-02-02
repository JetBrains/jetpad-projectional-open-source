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
package jetbrains.jetpad.completion;

import jetbrains.jetpad.base.Runnables;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CompletionMenuTest {
  private CompletionMenuModel model = new CompletionMenuModel();

  @Before
  public void init() {
    for (String s : Arrays.asList("aaa", "bbb", "aa")) {
      model.items.add(createItem(s));
    }
  }

  @Test
  public void initialState() {
    assertSelected("aa");
  }

  @Test
  public void itemFiltereing() {
    model.text.set("a");
    assertEquals(2, model.visibleItems.size());
  }

  @Test
  public void down() {
    model.down();
    assertSelected("aaa");
  }

  @Test
  public void up() {
    model.selectedItem.set(model.visibleItems.get(model.visibleItems.size() - 1));
    model.up();
    assertSelected("aaa");
  }

  private void assertSelected(String text) {
    assertEquals(text, model.selectedItem.get().visibleText(""));
  }

  private CompletionItem createItem(String text) {
    return new SimpleCompletionItem(text) {
      @Override
      public Runnable complete(String text) {
        return Runnables.EMPTY;
      }
    };
  }
}