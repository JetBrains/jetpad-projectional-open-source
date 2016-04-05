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
package jetbrains.jetpad.completion;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class BaseCompletionItemTestCase {
  private CompletionItem completionItem;

  @Before
  public void setup() {
    completionItem = createCompletionItem();
  }

  abstract CompletionItem createCompletionItem();

  @Test
  public void strictPrefixes() {
    for (String strictPrefix : getStrictPrefixes()) {
      assertTrue(strictPrefix, completionItem.isStrictMatchPrefix(strictPrefix));
      assertFalse(strictPrefix, completionItem.isMatch(strictPrefix));
    }
  }

  @Test
  public void matches() {
    for (String match : getMatches()) {
      assertFalse(match, completionItem.isStrictMatchPrefix(match));
      assertTrue(match, completionItem.isMatch(match));
    }
  }

  @Test
  public void badItems() {
    for (String bad : getBadItems()) {
      assertFalse(bad, completionItem.isStrictMatchPrefix(bad));
      assertFalse(bad, completionItem.isMatch(bad));
    }
  }

  abstract String[] getStrictPrefixes();

  abstract String[] getMatches();

  abstract String[] getBadItems();
}
