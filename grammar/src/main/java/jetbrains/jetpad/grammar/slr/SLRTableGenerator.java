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
package jetbrains.jetpad.grammar.slr;

import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.base.BaseLRTableGenerator;
import jetbrains.jetpad.grammar.base.LRActionRecord;
import jetbrains.jetpad.grammar.base.LRState;
import jetbrains.jetpad.grammar.base.LRTransition;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.*;

public class SLRTableGenerator extends BaseLRTableGenerator<SLRItem> {
  public SLRTableGenerator(Grammar grammar) {
    super(grammar);
  }

  protected List<LRState<SLRItem>> generateStates() {
    NonTerminal initial = grammar().getStart();
    if (initial.getRules().size() != 1) {
      throw new IllegalStateException("There should be one rule from inital non terminal");
    }

    Map<Set<SLRItem>, LRState<SLRItem>> states = new LinkedHashMap<>();

    int index = 0;
    LRState<SLRItem> init = new LRState<>(index++, closure(singleton(new SLRItem(initial.getFirstRule(), 0))));
    Set<LRState<SLRItem>> newItems = new LinkedHashSet<>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<LRState<SLRItem>> items = newItems;
      newItems = new LinkedHashSet<>();
      for (LRState<SLRItem> state : items) {
        for (Symbol s : grammar().getSymbols()) {
          Set<SLRItem> nextSet = nextSet(state.getItems(), s);
          if (nextSet.isEmpty()) continue;
          LRState<SLRItem> targetItem = states.get(nextSet);
          if (targetItem == null) {
            targetItem = new LRState<>(index++, nextSet);
            states.put(nextSet, targetItem);
            newItems.add(targetItem);
          }
          state.addTransition(new LRTransition<>(targetItem, s));
        }
      }
    }

    Rule firstRule = grammar().getStart().getFirstRule();
    final SLRItem finalItem = new SLRItem(firstRule, firstRule.getSymbols().size());
    for (LRState<SLRItem> state : states.values()) {
      for (SLRItem item : state.getItems()) {
        if (item.isFinal()) {
          for (Terminal t : item.getRule().getHead().getFollow()) {
            if (t == grammar().getEnd() && finalItem.equals(item)) {
              state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<SLRItem>>accept()));
            } else {
              state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<SLRItem>>reduce(item.getRule())));
            }
          }
        } else {
          Symbol s = item.getNextSymbol();
          LRState<SLRItem> nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            state.addRecord((Symbol) s, new LRActionRecord<>(item, LRParserAction.shift(nextState)));
          }
        }
      }
    }

    return new ArrayList<>(states.values());
  }

  private Set<SLRItem> closure(Set<SLRItem> items) {
    Set<SLRItem> result = new LinkedHashSet<>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      Set<SLRItem> toAdd = new LinkedHashSet<>();
      for (SLRItem item : result) {
        if (item.isFinal()) continue;
        if (!(item.getNextSymbol() instanceof NonTerminal)) continue;

        NonTerminal currentNonTerminal = (NonTerminal) item.getNextSymbol();
        for (Rule rule : currentNonTerminal.getRules()) {
          SLRItem newItem = new SLRItem(rule, 0);
          if (!result.contains(newItem)) {
            toAdd.add(newItem);
          }
        }
      }
      result.addAll(toAdd);
      hasChanges = !toAdd.isEmpty();
    }
    return result;
  }

  private Set<SLRItem> nextSet(Set<SLRItem> source, Symbol s) {
    Set<SLRItem> newSet = new LinkedHashSet<>();
    for (SLRItem item : source) {
      if (item.getNextSymbol() == s) {
        newSet.add(item.getNextItem());
      }
    }
    return closure(newSet);
  }
}