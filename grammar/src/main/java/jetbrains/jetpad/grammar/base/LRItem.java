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
package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;

/**
 * LRItem. It's a rule with a dot inside which specifies the current position of the parser in this rule.
 *
 * For example, if we have the following rule:
 * NT = T1 T2
 * We have the following valid positions:
 * NT = * T1 T2
 * NT = T1 * T2
 * NT = T1 T2 *
 */
public interface LRItem<ItemT extends LRItem<ItemT>> {
  /**
   * Rule which is associated with this item
   */
  Rule getRule();

  /**
   * Index of a position
   */
  int getIndex();

  /**
   * Whether the position has the form:
   * StartNT = * S1 S2 ... SN
   */
  boolean isKernel();

  /**
   * Initial if it has the form:
   * NT = * S1 ... SN
   */
  boolean isInitial();

  /**
   * Final if it has the form
   * NT = S1 ... SN *
   */
  boolean isFinal();

  /**
   * The symbol right after dot position
   */
  Symbol getNextSymbol();

  /**
   * The item which we get by moving dot by one position to the right
   */
  ItemT getNextItem();
}