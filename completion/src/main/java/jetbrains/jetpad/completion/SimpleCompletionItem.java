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

import javax.annotation.Nonnull;

public abstract class SimpleCompletionItem implements CompletionItem {
  private String myMatchingText;
  private String myVisibleText;

  protected SimpleCompletionItem(@Nonnull String matchingText) {
    this(matchingText, matchingText);
  }

  protected SimpleCompletionItem(@Nonnull String matchingText, @Nonnull String visibleText) {
    myMatchingText = matchingText;
    myVisibleText = visibleText;
  }

  protected boolean isCaseSensitive() {
    return true;
  }

  @Override
  public String visibleText(String text) {
    return myVisibleText;
  }

  @Override
  public boolean isStrictMatchPrefix(String text) {
    boolean startsWith = isCaseSensitive() ? myMatchingText.startsWith(text) : myMatchingText.toLowerCase().startsWith(text.toLowerCase());
    return startsWith && !isMatch(text);
  }

  @Override
  public boolean isMatch(String text) {
    if (isCaseSensitive()) {
      return myMatchingText.equals(text);
    } else {
      return myMatchingText.equalsIgnoreCase(text);
    }
  }

  @Override
  public String toString() {
    return "CompletionItem " + myMatchingText;
  }
}