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
package jetbrains.jetpad.grammar.slr;

import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;
import jetbrains.jetpad.grammar.base.LRItem;

import java.util.List;

class SLRItem implements LRItem<SLRItem> {
  private Rule myRule;
  private int myIndex;

  SLRItem(Rule rule, int index) {
    if (index < 0 || index > rule.getSymbols().size()) {
      throw new IllegalArgumentException();
    }

    myRule = rule;
    myIndex = index;
  }

  public Rule getRule() {
    return myRule;
  }

  public int getIndex() {
    return myIndex;
  }

  public boolean isKernel() {
    if (getRule().getHead() == getRule().getGrammar().getStart()) return true;
    return myIndex > 0;
  }

  public boolean isInitial() {
    return myIndex == 0;
  }

  public boolean isFinal() {
    return myIndex == myRule.getSymbols().size();
  }

  public Symbol getNextSymbol() {
    if (isFinal()) return null;
    return myRule.getSymbols().get(myIndex);
  }

  public SLRItem getNextItem() {
    if (getNextSymbol() == null) {
      throw new IllegalStateException();
    }
    return new SLRItem(myRule, myIndex + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SLRItem)) return false;

    SLRItem item = (SLRItem) obj;
    return item.myRule == myRule && item.myIndex == myIndex;
  }

  @Override
  public int hashCode() {
    return myRule.hashCode() * 31 + myIndex;
  }

  @Override
  public String toString() {
    String dot = "\u2022";
    String epsilon = "\0395";
    String arrow = "\u2192";

    StringBuilder result = new StringBuilder();
    result.append(myRule.getHead()).append(" ").append(arrow);
    List<Symbol> symbols = myRule.getSymbols();
    if (symbols.isEmpty()) {
      result.append(" ").append(dot).append(epsilon);
    } else {
      for (int i = 0; i < symbols.size(); i++) {
        result.append(" ");
        if (myIndex == i) {
          result.append(dot);
        }
        result.append(symbols.get(i));
      }
      if (myIndex == symbols.size()) {
        result.append(" ").append(dot);
      }
    }
    return result.toString();
  }
}