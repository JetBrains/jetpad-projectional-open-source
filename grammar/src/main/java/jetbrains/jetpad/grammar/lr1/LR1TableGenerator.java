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
package jetbrains.jetpad.grammar.lr1;

import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.base.LRActionRecord;
import jetbrains.jetpad.grammar.base.LRItem;
import jetbrains.jetpad.grammar.base.LRState;
import jetbrains.jetpad.grammar.base.LRTransition;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.singleton;

public class LR1TableGenerator {
  private Grammar myGrammar;

  public LR1TableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  public LRParserTable generateTable() {
    checkGrammar();


    final List<LRState<LR1Item>> states = generateStates();

    LRParserTable result = new LRParserTable(myGrammar);

    Map<LRState<LR1Item>, LRParserState> statesMap = new HashMap<>();
    statesMap.put(states.get(0), result.getInitialState());
    for (LRState<LR1Item> state : states) {
      if (state == states.get(0)) continue;
      statesMap.put(state, result.newState(state.getName()));
    }

    for (LRState<LR1Item> state : states) {
      LRParserState lrState = statesMap.get(state);

      for (LRTransition<LR1Item> trans : state.getTransitions()) {
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
          throw new IllegalStateException("There's ambiguity. Can't generate table");
        }
        LRActionRecord<LR1Item> rec = state.getRecord(t);

        LRParserAction<LRParserState> action;
        if (rec.getAction() instanceof LRParserAction.Shift<?>) {
          LRParserAction.Shift<LRState<LR1Item>> shift = (LRParserAction.Shift<LRState<LR1Item>>) rec.getAction();
          action = LRParserAction.shift(statesMap.get(shift.getState()));
        } else if (rec.getAction() instanceof LRParserAction.Reduce<?>) {
          LRParserAction.Reduce<LRState<LR1Item>> reduce = (LRParserAction.Reduce<LRState<LR1Item>>) rec.getAction();
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

  private List<LRState<LR1Item>> generateStates() {
    NonTerminal initial = myGrammar.getStart();
    if (initial.getRules().size() != 1) {
      throw new IllegalStateException("There should be one rule from inital non terminal");
    }

    Map<Set<LR1Item>, LRState<LR1Item>> states = new LinkedHashMap<>();

    int index = 0;
    LRState<LR1Item> init = new LRState<>(index++, closure(singleton(new LR1Item(initial.getFirstRule(), 0, myGrammar.getEnd()))));
    Set<LRState<LR1Item>> newItems = new LinkedHashSet<>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<LRState<LR1Item>> items = newItems;
      newItems = new LinkedHashSet<>();
      for (LRState<LR1Item> state : items) {
        for (Symbol s : myGrammar.getSymbols()) {
          Set<LR1Item> nextSet = nextSet(state.getItems(), s);
          if (nextSet.isEmpty()) continue;
          LRState<LR1Item> targetItem = states.get(nextSet);
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
    final LR1Item finalItem = new LR1Item(firstRule, firstRule.getSymbols().size(), myGrammar.getEnd());
    for (LRState<LR1Item> state : states.values()) {
      for (LR1Item item : state.getItems()) {
        if (item.isFinal()) {
          Terminal t = item.getLookAhead();
          if (t == myGrammar.getEnd() && finalItem.equals(item)) {
            state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<LR1Item>>accept()));
          } else {
            state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<LR1Item>>reduce(item.getRule())));
          }
        } else {
          Symbol s = item.getNextSymbol();
          LRState<LR1Item> nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            state.addRecord((Symbol) s, new LRActionRecord<>(item, LRParserAction.shift(nextState)));
          }
        }
      }
    }

    return new ArrayList<>(states.values());
  }

  private Set<LR1Item> closure(Set<LR1Item> items) {
    Set<LR1Item> result = new LinkedHashSet<>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      Set<LR1Item> toAdd = new LinkedHashSet<>();
      for (LR1Item item : result) {
        if (item.isFinal()) continue;
        if (!(item.getNextSymbol() instanceof NonTerminal)) continue;

        NonTerminal currentNonTerminal = (NonTerminal) item.getNextSymbol();
        List<Symbol> suffix = new ArrayList<>();
        List<Symbol> rightPart = item.getRule().getSymbols();
        suffix.addAll(rightPart.subList(item.getIndex() + 1, rightPart.size()));
        suffix.add(item.getLookAhead());
        Set<Terminal> first = first(suffix);
        for (Rule rule : currentNonTerminal.getRules()) {
          for (Terminal t : first) {
            LR1Item newItem = new LR1Item(rule, 0, t);
            if (!result.contains(newItem)) {
              toAdd.add(newItem);
            }
          }
        }
      }
      result.addAll(toAdd);
      hasChanges = !toAdd.isEmpty();
    }
    return result;
  }

  private Set<Terminal> first(List<Symbol> string) {
    Set<Terminal> result = new HashSet<>();
    for (Symbol s : string) {
      if (s instanceof Terminal) {
        result.add((Terminal) s);
        return result;
      } else if (s instanceof NonTerminal) {
        NonTerminal nt = (NonTerminal) s;
        result.addAll(nt.getFirst());
        if (!nt.isNullable()) return result;
      }
    }
    return result;
  }

  private Set<LR1Item> nextSet(Set<LR1Item> source, Symbol s) {
    Set<LR1Item> newSet = new LinkedHashSet<>();
    for (LR1Item item : source) {
      if (item.getNextSymbol() == s) {
        newSet.add(item.getNextItem());
      }
    }
    return closure(newSet);
  }

  public void dumpTable() {
    List<LRState<LR1Item>> states = generateStates();
    System.out.println("LR1 Table : \n");
    for (LRState<LR1Item> state : states) {
      System.out.println(state);
      System.out.println("Transitions:");
      for (LRTransition<LR1Item> t : state.getTransitions()) {
        System.out.println(t);
      }

      for (Terminal t : myGrammar.getTerminals()) {
        Set<LRActionRecord<LR1Item>> records = state.getMergedRecords(t);
        if (records.isEmpty()) continue;

        StringBuilder text = new StringBuilder();
        text.append("on ").append(t).append(" ");

        if (!state.hasAmbiguity(t)) {
          text.append(toString(records.iterator().next().getAction()));
        } else {
          List<String> actions = new ArrayList<>();
          for (LRActionRecord<LR1Item> rec : records) {
            actions.add(toString(rec.getAction()));
          }
          text.append(actions).append("  !CONFLICT!  ");
        }
        System.out.println(text);
      }

      System.out.println("");
    }
  }

  private String toString(LRParserAction<LRState<LR1Item>> action) {
    if (action instanceof LRParserAction.Shift) {
      return "shift " + ((LRParserAction.Shift<LRState<LR1Item>>) action).getState().getName();
    }
    return action.toString();
  }

}