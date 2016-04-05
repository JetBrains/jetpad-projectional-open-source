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


public interface CompletionItem {
  String visibleText(String text);

  /**
   * Returns true if there exist a match whose proper prefix is text
   */
  boolean isStrictMatchPrefix(String text);

  /**
   * isMatch(text) || isStrictMatchPrefix(text)
   */
  boolean isMatchPrefix(String text);

  /**
   * Returns is the string text can be matched
   */
  boolean isMatch(String text);

  /**
   * Match priority matters when we have > 1 strictly matching items with different priorities - a subset with
   * the highest priority wins. One of use cases is supporting variable references and keyword variables. If
   * there's a keyword expression, it beats variable reference.
   * The greater is returned number, the higher is priority.
   */
  int getMatchPriority();

  /**
   * Use this priority to move items higher in completion menu. Default priority should be 0. The higher
   * the priority, the higher in completion menu the item will be.
   */
  int getSortPriority();

  Runnable complete(String text);
}