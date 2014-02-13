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
import jetbrains.jetpad.grammar.base.LRActionRecord;
import jetbrains.jetpad.grammar.base.LRState;
import jetbrains.jetpad.grammar.base.LRTransition;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.*;

public class SLRTableGenerator {
  private Grammar myGrammar;

  public SLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  public LRParserTable generateTable() {
    checkGrammar();

    final List<LRState<SLRItem>> states = generateStates();

    LRParserTable result = new LRParserTable(myGrammar);

    Map<LRState<SLRItem>, LRParserState> statesMap = new HashMap<>();
    statesMap.put(states.get(0), result.getInitialState());
    for (LRState<SLRItem> state : states) {
      if (state == states.get(0)) continue;
      statesMap.put(state, result.newState(state.getName()));
    }

    for (LRState<SLRItem> state : states) {
      LRParserState lrState = statesMap.get(state);

      for (LRTransition<SLRItem> trans : state.getTransitions()) {
        if (trans.getSymbol() instanceof NonTerminal) {
          NonTerminal nt = (NonTerminal) trans.getSymbol();
          lrState.addNextState(nt, statesMap.get(trans.getTarget()));
        }
      }

      Map<Terminal, Set<LRParserAction<LRParserState>>> actions = new LinkedHashMap<>();
      for (Terminal s : myGrammar.getTerminals()) {
        actions.put(s, new LinkedHashSet<LRParserAction<LRParserState>>());
      }

      for (Symbol s : myGrammar.getSymbols()) {
        if (!(s instanceof Terminal)) continue;

        Terminal t = (Terminal) s;

        if (!state.hasRecords(s)) continue;

        if (state.hasAmbiguity(t)) {
          throw new IllegalStateException("There's ambiguity. Can't tgenerate table");
        }
        LRActionRecord<SLRItem> rec = state.getRecord(t);

        LRParserAction<LRParserState> action;
        if (rec.getAction() instanceof LRParserAction.Shift<?>) {
          LRParserAction.Shift<LRState<SLRItem>> shift = (LRParserAction.Shift<LRState<SLRItem>>) rec.getAction();
          action = LRParserAction.shift(statesMap.get(shift.getState()));
        } else if (rec.getAction() instanceof LRParserAction.Reduce<?>) {
          LRParserAction.Reduce<LRState<SLRItem>> reduce = (LRParserAction.Reduce<LRState<SLRItem>>) rec.getAction();
          action = LRParserAction.reduce(reduce.getRule());
        } else if (rec.getAction() instanceof LRParserAction.Accept<?>) {
          action = LRParserAction.accept();
        } else if (rec.getAction() instanceof LRParserAction.Error<?>) {
          action = LRParserAction.error();
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

  private List<LRState<SLRItem>> generateStates() {
    NonTerminal initial = myGrammar.getStart();
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
        for (Symbol s : myGrammar.getSymbols()) {
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

    Rule firstRule = myGrammar.getStart().getFirstRule();
    final SLRItem finalItem = new SLRItem(firstRule, firstRule.getSymbols().size());
    for (LRState<SLRItem> state : states.values()) {
      for (SLRItem item : state.getItems()) {
        if (item.isFinal()) {
          for (Terminal t : item.getRule().getHead().getFollow()) {
            if (t == myGrammar.getEnd() && finalItem.equals(item)) {
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

  public void dumpTable() {
    List<LRState<SLRItem>> states = generateStates();
    System.out.println("SLR Table : \n");
    for (LRState<SLRItem> state : states) {
      System.out.println(state);
      System.out.println("Transitions:");
      for (LRTransition<SLRItem> t : state.getTransitions()) {
        System.out.println(t);
      }

      for (Terminal t : myGrammar.getTerminals()) {
        Set<LRActionRecord<SLRItem>> records = state.getRecords(t);
        if (records.isEmpty()) continue;

        StringBuilder text = new StringBuilder();
        text.append("on ").append(t).append(" ");

        if (!state.hasAmbiguity(t)) {
          text.append(toString(records.iterator().next().getAction()));
        } else {
          List<String> actions = new ArrayList<>();
          for (LRActionRecord<SLRItem> rec : records) {
            actions.add(toString(rec.getAction()));
          }
          text.append(actions).append("  !CONFLICT!  ");
        }
        System.out.println(text);
      }

      System.out.println("");
    }
  }

  private String toString(LRParserAction<LRState<SLRItem>> action) {
    if (action instanceof LRParserAction.Shift) {
      return "shift " + ((LRParserAction.Shift<LRState<SLRItem>>) action).getState().getName();
    }
    return action.toString();
  }

}