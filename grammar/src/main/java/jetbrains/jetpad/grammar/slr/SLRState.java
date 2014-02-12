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

import jetbrains.jetpad.grammar.Symbol;

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