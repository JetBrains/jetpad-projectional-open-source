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

public class WrapperCompletionItem implements CompletionItem {
  private CompletionItem myItem;

  public WrapperCompletionItem(CompletionItem item) {
    myItem = item;
  }

  @Override
  public String visibleText(String text) {
    return myItem.visibleText(text);
  }

  @Override
  public boolean isStrictMatchPrefix(String text) {
    return myItem.isStrictMatchPrefix(text);
  }

  @Override
  public boolean isMatch(String text) {
    return myItem.isMatch(text);
  }

  @Override
  public boolean isMatchPrefix(String text) {
    return myItem.isMatchPrefix(text);
  }

  @Override
  public int getMatchPriority() {
    return myItem.getMatchPriority();
  }

  @Override
  public int getSortPriority() {
    return myItem.getSortPriority();
  }

  @Override
  public Runnable complete(String text) {
    return myItem.complete(text);
  }

  @Override
  public String toString() {
    return "Wrapper { " + myItem + " }";
  }
}