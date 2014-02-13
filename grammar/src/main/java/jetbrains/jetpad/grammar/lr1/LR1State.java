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

import jetbrains.jetpad.grammar.Associativity;
import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;
import jetbrains.jetpad.grammar.lr.LRAction;

import java.util.*;

class LR1State {
  private int myNumber;
  private Set<LR1Item> myItems = new LinkedHashSet<>();
  private Map<Symbol, Set<LR1ActionRecord>> myActionRecords = new HashMap<>();

  private Set<LR1Transition> myTransitions = new LinkedHashSet<>();

  LR1State(int number, Set<LR1Item> items) {
    myNumber = number;
    myItems.addAll(items);
  }

  String getName() {
    return "S" + myNumber;
  }

  int getNumber() {
    return myNumber;
  }

  Set<LR1Item> getItems() {
    return Collections.unmodifiableSet(myItems);
  }

  Set<LR1Item> getKernelItems() {
    Set<LR1Item> items = new LinkedHashSet<>();
    for (LR1Item item : myItems) {
      if (item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  Set<LR1Item> getNonKernelItems() {
    Set<LR1Item> items = new LinkedHashSet<>();
    for (LR1Item item : myItems) {
      if (!item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  Set<LR1Transition> getTransitions() {
    return Collections.unmodifiableSet(myTransitions);
  }

  Set<LR1ActionRecord> getRecords(Symbol s) {
    Set<LR1ActionRecord> records = myActionRecords.get(s);
    if (records == null) return Collections.emptySet();
    return Collections.unmodifiableSet(records);
  }

  Set<LR1ActionRecord> getMergedRecords(Symbol s) {
    return mergeActions(getRecords(s));
  }

  boolean hasRecords(Symbol s) {
    return myActionRecords.get(s) != null && !myActionRecords.get(s).isEmpty();
  }

  boolean hasAmbiguity(Symbol s) {
    Set<LR1ActionRecord> records = getRecords(s);
    if (records.size() == 1) return false;
    return disambiguate(records) == null;
  }

  LR1ActionRecord getRecord(Symbol s) {
    Set<LR1ActionRecord> records = getRecords(s);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    LR1ActionRecord result = disambiguate(records);
    if (result == null) {
      throw new IllegalStateException("There's ambiguity");
    }
    return result;
  }

  LR1State getState(Symbol symbol) {
    for (LR1Transition t : myTransitions) {
      if (t.getSymbol() == symbol) return t.getTarget();
    }
    return null;
  }

  void addTransition(LR1Transition t) {
    myTransitions.add(t);
  }

  void addRecord(Symbol s, LR1ActionRecord rec) {
    if (!myActionRecords.containsKey(s)) {
      myActionRecords.put(s, new HashSet<LR1ActionRecord>());
    }
    myActionRecords.get(s).add(rec);
  }

  private LR1ActionRecord disambiguate(Set<LR1ActionRecord> records) {
    records = mergeActions(records); //todo need a test for this ambiguity (it happens in dot operation between .id and .id(args))
    records = filterByPriority(records);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    LR1ActionRecord result = disambiguateByAssoc(records);
    if (result != null) return result;
    return null;
  }

  private Set<LR1ActionRecord> mergeActions(Set<LR1ActionRecord> records) {
    Set<LR1ActionRecord> result = new HashSet<>();
    Map<LRAction<LR1State>, LR1ActionRecord> actions = new HashMap<>();

    for (LR1ActionRecord r : records) {
      if (actions.containsKey(r.getAction())) {
        actions.get(r.getAction()).addDuplicate(r);
        continue;
      }
      result.add(r);
      actions.put(r.getAction(), r);
    }

    return result;
  }

  private Set<LR1ActionRecord> filterByPriority(Set<LR1ActionRecord> records) {
    Integer highestPriority = null;
    for (LR1ActionRecord rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (currentPriority == null) return records;
      if (highestPriority == null) {
        highestPriority = currentPriority;
      } else {
        highestPriority = Math.max(highestPriority, currentPriority);
      }
    }

    Set<LR1ActionRecord> result = new HashSet<>();
    for (LR1ActionRecord rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (com.google.common.base.Objects.equal(currentPriority, highestPriority)) {
        result.add(rec);
      }
    }
    return result;
  }

  private LR1ActionRecord disambiguateByAssoc(Set<LR1ActionRecord> records) {
    Integer priority = null;
    Rule rule = null;
    for (LR1ActionRecord rec : records) {
      Integer cp = rec.getItem().getRule().getPriority();
      if (cp == null) return null;
      if (priority == null) {
        priority = cp;
        rule = rec.getItem().getRule();
      } else if (!cp.equals(priority)) {
        return null;
      }
    }

    Associativity assoc = rule.getAssociativity();
    if (assoc == null) return null;

    LR1ActionRecord bestRecord = records.iterator().next();
    for (LR1ActionRecord rec : records) {
      if (assoc == Associativity.LEFT) {
        if (rec.getItem().getIndex() > bestRecord.getItem().getIndex()) {
          bestRecord = rec;
        }
      } else {
        if (rec.getItem().getIndex() < bestRecord.getItem().getIndex()) {
          bestRecord = rec;
        }
      }
    }

    return bestRecord;
  }

  @Override
  public int hashCode() {
    return myItems.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LR1State)) return false;

    LR1State otherState = (LR1State) obj;
    return myItems.equals(otherState.myItems);
  }

  @Override
  public String toString() {
    return getName() + " : " + getKernelItems() + " / " + getItems();
  }
}