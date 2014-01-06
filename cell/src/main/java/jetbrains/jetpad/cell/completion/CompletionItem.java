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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.cell.action.CellAction;

public interface CompletionItem {
  String visibleText(String text);

  /**
   * Returns if there exist a match whose non trivial prefix is text
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
   * Low priority means that if we have two strictly matching items one with low priority and another without it, then the higher
   * priority item beats the lower priority item. We need this in order to support variable references and keyword variables. If
   * there's a keyword expression, it beats variable reference.
   */
  boolean isLowPriority();

  CellAction complete(String text);
}