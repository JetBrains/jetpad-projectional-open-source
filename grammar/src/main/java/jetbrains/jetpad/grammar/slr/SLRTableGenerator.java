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

import com.google.common.base.Objects;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.lr.LRAction;
import jetbrains.jetpad.grammar.lr.LRState;
import jetbrains.jetpad.grammar.lr.LRTable;

import java.util.*;

import static java.util.Collections.*;

public class SLRTableGenerator {
  private Grammar myGrammar;

  public SLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  public LRTable generateTable() {
    checkGrammar();

    final List<SLRState> states = generateStates();

    LRTable result = new LRTable(myGrammar);

    Map<SLRState, LRState> statesMap = new HashMap<>();
    statesMap.put(states.get(0), result.getInitialState());
    for (SLRState state : states) {
      if (state == states.get(0)) continue;
      statesMap.put(state, result.newState(state.getName()));
    }

    for (SLRState state : states) {
      LRState lrState = statesMap.get(state);

      for (SLRTransition trans : state.getTransitions()) {
        if (trans.getSymbol() instanceof NonTerminal) {
          NonTerminal nt = (NonTerminal) trans.getSymbol();
          lrState.addNextState(nt, statesMap.get(trans.getTarget()));
        }
      }

      Map<Terminal, Set<LRAction<LRState>>> actions = new LinkedHashMap<>();
      for (Terminal s : myGrammar.getTerminals()) {
        actions.put(s, new LinkedHashSet<LRAction<LRState>>());
      }

      for (Symbol s : myGrammar.getSymbols()) {
        if (!(s instanceof Terminal)) continue;

        Terminal t = (Terminal) s;

        if (!state.hasRecords(s)) continue;

        if (state.hasAmbiguity(t)) {
          throw new IllegalStateException("There's ambiguity. Can't tgenerate table");
        }
        SLRActionRecord rec = state.getRecord(t);

        LRAction<LRState> action;
        if (rec.getAction() instanceof LRAction.Shift<?>) {
          LRAction.Shift<SLRState> shift = (LRAction.Shift<SLRState>) rec.getAction();
          action = LRAction.shift(statesMap.get(shift.getState()));
        } else if (rec.getAction() instanceof LRAction.Reduce<?>) {
          LRAction.Reduce<SLRState> reduce = (LRAction.Reduce<SLRState>) rec.getAction();
          action = LRAction.reduce(reduce.getRule());
        } else if (rec.getAction() instanceof LRAction.Accept<?>) {
          action = LRAction.accept();
        } else if (rec.getAction() instanceof LRAction.Error<?>) {
          action = LRAction.error();
        } else {
          throw new IllegalStateException();
        }

        lrState.addAction(t, action);
      }
    }

    return result;
  }

  private void checkGrammar() {
    NonTerminal start = myGrammar.getStart();
    if (start.getRules().size() != 1) throw new IllegalArgumentException();
    Rule firstRule = start.getRules().iterator().next();
    if (firstRule.getSymbols().size() != 1) throw new IllegalArgumentException();
    if (!(firstRule.getSymbols().get(0) instanceof NonTerminal)) throw new IllegalArgumentException();
  }

  private List<SLRState> generateStates() {
    NonTerminal initial = myGrammar.getStart();
    if (initial.getRules().size() != 1) {
      throw new IllegalStateException("There should be one rule from inital non terminal");
    }

    Map<Set<SLRItem>, SLRState> states = new LinkedHashMap<>();

    int index = 0;
    SLRState init = new SLRState(index++, closure(singleton(new SLRItem(initial.getFirstRule(), 0))));
    Set<SLRState> newItems = new LinkedHashSet<>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<SLRState> items = newItems;
      newItems = new LinkedHashSet<>();
      for (SLRState state : items) {
        for (Symbol s : myGrammar.getSymbols()) {
          Set<SLRItem> nextSet = nextSet(state.getItems(), s);
          if (nextSet.isEmpty()) continue;
          SLRState targetItem = states.get(nextSet);
          if (targetItem == null) {
            targetItem = new SLRState(index++, nextSet);
            states.put(nextSet, targetItem);
            newItems.add(targetItem);
          }
          state.addTransition(new SLRTransition(targetItem, s));
        }
      }
    }

    Rule firstRule = myGrammar.getStart().getFirstRule();
    final SLRItem finalItem = new SLRItem(firstRule, firstRule.getSymbols().size());
    for (SLRState state : states.values()) {
      for (SLRItem item : state.getItems()) {
        if (item.isFinal()) {
          for (Terminal t : item.getRule().getHead().getFollow()) {
            if (t == myGrammar.getEnd() && finalItem.equals(item)) {
              state.addRecord(t, new SLRActionRecord(item, LRAction.<SLRState>accept()));
            } else {
              state.addRecord(t, new SLRActionRecord(item, LRAction.<SLRState>reduce(item.getRule())));
            }
          }
        } else {
          Symbol s = item.getNextSymbol();
          SLRState nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            state.addRecord((Symbol) s, new SLRActionRecord(item, LRAction.shift(nextState)));
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

  public void dumpTable() {
    List<SLRState> states = generateStates();
    System.out.println("SLR Table : \n");
    for (SLRState state : states) {
      System.out.println(state);
      System.out.println("Transitions:");
      for (SLRTransition t : state.getTransitions()) {
        System.out.println(t);
      }

      System.out.println("Actions:");

      Map<Symbol, Set<String>> actions = new LinkedHashMap<>();
      for (Symbol s : myGrammar.getSymbols()) {
        actions.put(s, new LinkedHashSet<String>());
      }

      for (SLRItem item : state.getItems()) {
        if (item.isFinal()) {
          for (Terminal t : item.getRule().getHead().getFollow()) {
            actions.get(t).add("reduce " + item.getRule());
          }
        } else {
          Symbol s = item.getNextSymbol();
          SLRState nextState = state.getState(s);
          if (nextState != null) {
            actions.get(s).add("shift " + nextState.getName());
          }
        }
      }

      for (Map.Entry<Symbol, Set<String>> e : actions.entrySet()) {
        if (e.getValue().isEmpty()) continue;

        System.out.print("on " + e.getKey() + " : " + e.getValue());

        if (e.getValue().size() > 1) {
          System.out.print("  !!!Conflict!!!");
        }

        System.out.println();
      }

      System.out.println("");
    }
  }

}