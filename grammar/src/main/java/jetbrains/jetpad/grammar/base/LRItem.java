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
package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;

public abstract interface LRItem<ItemT extends LRItem<ItemT>> {
  Rule getRule();
  int getIndex();
  boolean isKernel();
  boolean isInitial();
  boolean isFinal();
  Symbol getNextSymbol();
  ItemT getNextItem();
}