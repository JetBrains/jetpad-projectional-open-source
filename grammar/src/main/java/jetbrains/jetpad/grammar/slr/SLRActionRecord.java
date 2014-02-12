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

import jetbrains.jetpad.grammar.lr.LRAction;
import jetbrains.jetpad.grammar.lr.LRState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SLRActionRecord {
  private SLRItem myItem;
  private LRAction<SLRState> myAction;
  private Set<SLRActionRecord> duplicates = new HashSet<>();

  SLRActionRecord(SLRItem item, LRAction<SLRState> action) {
    this.myItem = item;
    this.myAction = action;
  }

  SLRItem getItem() {
    return myItem;
  }

  LRAction<SLRState> getAction() {
    return myAction;
  }

  void addDuplicate(SLRActionRecord rec) {
    duplicates.add(rec);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    List<SLRItem> items = new ArrayList<>();
    items.add(myItem);
    for (SLRActionRecord r : duplicates) {
      items.add(r.myItem);
    }
    result.append(items).append(" : ").append(myAction);
    return result.toString();
  }
}