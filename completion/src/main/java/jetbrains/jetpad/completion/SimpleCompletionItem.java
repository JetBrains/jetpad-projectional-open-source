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
package jetbrains.jetpad.completion;

import jetbrains.jetpad.completion.BaseCompletionItem;

import javax.validation.constraints.NotNull;


public abstract class SimpleCompletionItem extends BaseCompletionItem {
  private String myMatchingText;
  private String myVisibleText;

  protected SimpleCompletionItem(@NotNull String matchingText) {
    this(matchingText, matchingText);
  }

  protected SimpleCompletionItem(@NotNull String matchingText, @NotNull String visibleText) {
    if (matchingText == null) throw new NullPointerException();
    if (visibleText == null) throw new NullPointerException();
    myMatchingText = matchingText;
    myVisibleText = visibleText;
  }

  @Override
  public String visibleText(String text) {
    return myVisibleText;
  }

  @Override
  public boolean isStrictMatchPrefix(String text) {
    return myMatchingText.startsWith(text) && !isMatch(text);
  }

  @Override
  public boolean isMatch(String text) {
    return myMatchingText.equals(text);
  }

  @Override
  public String toString() {
    return "CompletionItem " + myMatchingText;
  }
}