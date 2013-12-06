/*
 * Copyright 2012-2013 JetBrains s.r.o
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

    Rule firstRule = myGrammar.getStart().getFirstRule();
    final SLRItem finalItem = new SLRItem(firstRule, firstRule.getSymbols().size());

    LRTable result = new LRTable(myGrammar);

    Map<SLRState, LRState> statesMap = new HashMap<SLRState, LRState>();
    statesMap.put(states.get(0), result.getInitialState());
    for (SLRState state : states) {
      if (state == states.get(0)) continue;
      statesMap.put(state, result.newState(state.getName()));
    }

    for (SLRState state : states) {
      LRState lrState = statesMap.get(state);

      Map<Terminal, Set<ActionRecord>> actions = new LinkedHashMap<Terminal, Set<ActionRecord>>();

      for (NonTerminal nt : myGrammar.getNonTerminals()) {
        SLRState nextState = state.getState(nt);
        if (nextState != null) {
          lrState.addNextState(nt, statesMap.get(nextState));
        }
      }

      for (Terminal s : myGrammar.getTerminals()) {
        actions.put(s, new LinkedHashSet<ActionRecord>());
      }

      for (SLRItem item : state.getItems()) {
        if (item.isFinal()) {
          for (Terminal t : item.getRule().getHead().getFollow()) {
            if (t == myGrammar.getEnd() && finalItem.equals(item)) {
              actions.get(t).add(new ActionRecord(item, LRAction.accept()));
            } else {
              actions.get(t).add(new ActionRecord(item, LRAction.reduce(item.getRule())));
            }
          }
        } else {
          Symbol s = item.getNextSymbol();
          SLRState nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            actions.get((Terminal) s).add(new ActionRecord(item, LRAction.shift(statesMap.get(nextState))));
          }
        }
      }

      for (Map.Entry<Terminal, Set<ActionRecord>> e : actions.entrySet()) {
        if (e.getValue().isEmpty()) continue;
        if (e.getValue().size() > 1) {
          ActionRecord record = disambiguate(e.getValue());
          if (record == null) {
            disambiguate(e.getValue());
            throw new IllegalStateException("There's a conflict");
          } else {
            lrState.addAction(e.getKey(), record.action);
          }
        } else {
          lrState.addAction(e.getKey(), e.getValue().iterator().next().action);
        }
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


  private ActionRecord disambiguate(Set<ActionRecord> records) {
    records = mergeActions(records); //todo need a test for this ambiguity (it happens in dot operation between .id nad .id(args))
    records = filterByPriority(records);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    ActionRecord result = disambiguateByAssoc(records);
    if (result != null) return result;
    return null;
  }

  private Set<ActionRecord> mergeActions(Set<ActionRecord> records) {
    Set<ActionRecord> result = new HashSet<ActionRecord>();
    Map<LRAction, ActionRecord> actions = new HashMap<LRAction, ActionRecord>();

    for (ActionRecord r : records) {
      if (actions.containsKey(r.action)) {
        actions.get(r.action).addDuplciate(r);
        continue;
      }
      result.add(r);
      actions.put(r.action, r);
    }

    return result;
  }

  private Set<ActionRecord> filterByPriority(Set<ActionRecord> records) {
    Integer highestPriority = null;
    for (ActionRecord rec : records) {
      Integer currentPriority = rec.item.getRule().getPriority();
      if (currentPriority == null) return records;
      if (highestPriority == null) {
        highestPriority = currentPriority;
      } else {
        highestPriority = Math.max(highestPriority, currentPriority);
      }
    }

    Set<ActionRecord> result = new HashSet<ActionRecord>();
    for (ActionRecord rec : records) {
      Integer currentPriority = rec.item.getRule().getPriority();
      if (Objects.equal(currentPriority, highestPriority)) {
        result.add(rec);
      }
    }
    return result;
  }

  private ActionRecord disambiguateByAssoc(Set<ActionRecord> records) {
    Integer priority = null;
    Rule rule = null;
    for (ActionRecord rec : records) {
      Integer cp = rec.item.getRule().getPriority();
      if (cp == null) return null;
      if (priority == null) {
        priority = cp;
        rule = rec.item.getRule();
      } else if (!cp.equals(priority)) {
        return null;
      }
    }

    Associativity assoc = rule.getAssociativity();
    if (assoc == null) return null;

    ActionRecord bestRecord = records.iterator().next();
    for (ActionRecord rec : records) {
      if (assoc == Associativity.LEFT) {
        if (rec.item.getIndex() > bestRecord.item.getIndex()) {
          bestRecord = rec;
        }
      } else {
        if (rec.item.getIndex() < bestRecord.item.getIndex()) {
          bestRecord = rec;
        }
      }
    }

    return bestRecord;
  }

  private List<SLRState> generateStates() {
    NonTerminal initial = myGrammar.getStart();
    if (initial.getRules().size() != 1) throw new IllegalStateException();

    Map<Set<SLRItem>, SLRState> states = new LinkedHashMap<Set<SLRItem>, SLRState>();

    int index = 0;
    SLRState init = new SLRState(index++, closure(singleton(new SLRItem(initial.getFirstRule(), 0))));
    Set<SLRState> newItems = new LinkedHashSet<SLRState>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<SLRState> items = newItems;
      newItems = new LinkedHashSet<SLRState>();
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

    return new ArrayList<SLRState>(states.values());
  }

  private Set<SLRItem> closure(Set<SLRItem> items) {
    Set<SLRItem> result = new LinkedHashSet<SLRItem>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      Set<SLRItem> toAdd = new LinkedHashSet<SLRItem>();
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
    Set<SLRItem> newSet = new LinkedHashSet<SLRItem>();
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

      Map<Symbol, Set<String>> actions = new LinkedHashMap<Symbol, Set<String>>();
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

  private class ActionRecord {
    final SLRItem item;
    final LRAction action;
    private Set<ActionRecord> duplicates = new HashSet<ActionRecord>();

    ActionRecord(SLRItem item, LRAction action) {
      this.item = item;
      this.action = action;
    }

    void addDuplciate(ActionRecord rec) {
      duplicates.add(rec);
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      List<SLRItem> items = new ArrayList<SLRItem>();
      items.add(item);
      for (ActionRecord r : duplicates) {
        items.add(r.item);
      }
      result.append(items).append(" : ").append(action);
      return result.toString();
    }
  }
}