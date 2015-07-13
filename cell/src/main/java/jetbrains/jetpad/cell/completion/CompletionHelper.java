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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompletionHelper {
  public static CompletionHelper completionFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, Completion.COMPLETION);
  }

  public static CompletionHelper rightTransformFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, Completion.RIGHT_TRANSFORM);
  }

  public static CompletionHelper leftTransformFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, Completion.LEFT_TRANSFORM);
  }

  public static CompletionHelper completionFor(Cell cell, CompletionParameters cp, CellTraitPropertySpec<CompletionSupplier> prop) {
    return new CompletionHelper(cell.get(prop), cp);
  }

  private CompletionSupplier mySupplier;
  private CompletionParameters myParameters;
  private List<CompletionItem> myCachedItems;

  public CompletionHelper(CompletionSupplier supplier, CompletionParameters params) {
    mySupplier = supplier;
    myParameters = params;
  }

  public boolean isEmpty() {
    return mySupplier.get(myParameters).isEmpty();
  }

  public List<CompletionItem> getItems() {
    if (myCachedItems == null) {
      myCachedItems = Collections.unmodifiableList(mySupplier.get(myParameters));
    }
    return myCachedItems;
  }

  public List<CompletionItem> prefixedBy(String prefix) {
    List<CompletionItem> result = new ArrayList<>();
    for (CompletionItem item : getItems()) {
      if (item.isMatchPrefix(prefix)) {
        result.add(item);
      }
    }
    return result;
  }

  public List<CompletionItem> strictlyPrefixedBy(String prefix) {
    List<CompletionItem> result = new ArrayList<>();
    for (CompletionItem item : getItems()) {
      if (item.isStrictMatchPrefix(prefix)) {
        result.add(item);
      }
    }
    return result;
  }

  public List<CompletionItem> matches(String text) {
    List<CompletionItem> result = new ArrayList<>();
    for (CompletionItem item : getItems()) {
      if (item.isMatch(text)) {
        result.add(item);
      }
    }
    return reduce(result);
  }

  private List<CompletionItem> reduce(List<CompletionItem> items) {
    List<CompletionItem> lowPriority = new ArrayList<>();
    List<CompletionItem> result = new ArrayList<>();
    for (CompletionItem item : items) {
      if (item.isLowMatchPriority()) {
        lowPriority.add(item);
      } else {
        result.add(item);
      }
    }
    if (result.isEmpty()) return lowPriority;
    return result;
  }

  public boolean hasSingleMatch(String text, boolean eager) {
    List<CompletionItem> matches = matches(text);
    List<CompletionItem> strictlyPrefixed = strictlyPrefixedBy(text);

    if (matches.size() == 1) {
      if (eager) return true;
      if (matches.containsAll(strictlyPrefixed)) return true;

      List<CompletionItem> allItems = new ArrayList<>();
      allItems.addAll(matches);
      allItems.addAll(strictlyPrefixed);

      List<CompletionItem> reduced = reduce(allItems);
      if (reduced.size() == 1 && reduced.containsAll(matches)) return true;
    }
    return false;
  }

  public boolean isBoundary(String text, int position) {
    if (position == 0 || position == text.length()) return false;

    String prefix = text.substring(0, position);
    String prefixPlusOne = text.substring(0, position + 1);

    return matches(prefix).size() == 1 && prefixedBy(prefixPlusOne).isEmpty();
  }

  public void completeFirstMatch(String text) {
    matches(text).get(0).complete(text).run();
  }

  public boolean hasMatches(String text) {
    return !prefixedBy(text).isEmpty();
  }
}