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

import jetbrains.jetpad.grammar.Associativity;
import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;
import jetbrains.jetpad.grammar.parser.LRAction;

import java.util.*;

class SLRState {
  private int myNumber;
  private Set<SLRItem> myItems = new LinkedHashSet<>();
  private Map<Symbol, Set<SLRActionRecord>> myActionRecords = new HashMap<>();

  private Set<SLRTransition> myTransitions = new LinkedHashSet<>();

  SLRState(int number, Set<SLRItem> items) {
    myNumber = number;
    myItems.addAll(items);
  }

  String getName() {
    return "S" + myNumber;
  }

  int getNumber() {
    return myNumber;
  }

  Set<SLRItem> getItems() {
    return Collections.unmodifiableSet(myItems);
  }

  Set<SLRItem> getKernelItems() {
    Set<SLRItem> items = new LinkedHashSet<>();
    for (SLRItem item : myItems) {
      if (item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  Set<SLRItem> getNonKernelItems() {
    Set<SLRItem> items = new LinkedHashSet<>();
    for (SLRItem item : myItems) {
      if (!item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  Set<SLRTransition> getTransitions() {
    return Collections.unmodifiableSet(myTransitions);
  }

  Set<SLRActionRecord> getRecords(Symbol s) {
    Set<SLRActionRecord> records = myActionRecords.get(s);
    if (records == null) return Collections.emptySet();
    return Collections.unmodifiableSet(records);
  }

  Set<SLRActionRecord> getMergedRecords(Symbol s) {
    return mergeActions(getRecords(s));
  }

  boolean hasRecords(Symbol s) {
    return myActionRecords.get(s) != null && !myActionRecords.get(s).isEmpty();
  }

  boolean hasAmbiguity(Symbol s) {
    Set<SLRActionRecord> records = getRecords(s);
    if (records.size() == 1) return false;
    return disambiguate(records) == null;
  }

  SLRActionRecord getRecord(Symbol s) {
    Set<SLRActionRecord> records = getRecords(s);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    SLRActionRecord result = disambiguate(records);
    if (result == null) {
      throw new IllegalStateException("There's ambiguity");
    }
    return result;
  }

  SLRState getState(Symbol symbol) {
    for (SLRTransition t : myTransitions) {
      if (t.getSymbol() == symbol) return t.getTarget();
    }
    return null;
  }

  void addTransition(SLRTransition t) {
    myTransitions.add(t);
  }

  void addRecord(Symbol s, SLRActionRecord rec) {
    if (!myActionRecords.containsKey(s)) {
      myActionRecords.put(s, new HashSet<SLRActionRecord>());
    }
    myActionRecords.get(s).add(rec);
  }

  private SLRActionRecord disambiguate(Set<SLRActionRecord> records) {
    records = mergeActions(records); //todo need a test for this ambiguity (it happens in dot operation between .id and .id(args))
    records = filterByPriority(records);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    SLRActionRecord result = disambiguateByAssoc(records);
    if (result != null) return result;
    return null;
  }

  private Set<SLRActionRecord> mergeActions(Set<SLRActionRecord> records) {
    Set<SLRActionRecord> result = new HashSet<>();
    Map<LRAction<SLRState>, SLRActionRecord> actions = new HashMap<>();

    for (SLRActionRecord r : records) {
      if (actions.containsKey(r.getAction())) {
        actions.get(r.getAction()).addDuplicate(r);
        continue;
      }
      result.add(r);
      actions.put(r.getAction(), r);
    }

    return result;
  }

  private Set<SLRActionRecord> filterByPriority(Set<SLRActionRecord> records) {
    Integer highestPriority = null;
    for (SLRActionRecord rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (currentPriority == null) return records;
      if (highestPriority == null) {
        highestPriority = currentPriority;
      } else {
        highestPriority = Math.max(highestPriority, currentPriority);
      }
    }

    Set<SLRActionRecord> result = new HashSet<>();
    for (SLRActionRecord rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (com.google.common.base.Objects.equal(currentPriority, highestPriority)) {
        result.add(rec);
      }
    }
    return result;
  }

  private SLRActionRecord disambiguateByAssoc(Set<SLRActionRecord> records) {
    Integer priority = null;
    Rule rule = null;
    for (SLRActionRecord rec : records) {
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

    SLRActionRecord bestRecord = records.iterator().next();
    for (SLRActionRecord rec : records) {
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
    if (!(obj instanceof SLRState)) return false;

    SLRState otherState = (SLRState) obj;
    return myItems.equals(otherState.myItems);
  }

  @Override
  public String toString() {
    return getName() + " : " + getKernelItems()  + " / " + getNonKernelItems();
  }
}