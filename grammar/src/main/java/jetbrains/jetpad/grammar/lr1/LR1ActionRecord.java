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

import jetbrains.jetpad.grammar.parser.LRAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class LR1ActionRecord {
  private LR1Item myItem;
  private LRAction<LR1State> myAction;
  private Set<LR1ActionRecord> duplicates = new HashSet<>();

  LR1ActionRecord(LR1Item item, LRAction<LR1State> action) {
    this.myItem = item;
    this.myAction = action;
  }

  LR1Item getItem() {
    return myItem;
  }

  LRAction<LR1State> getAction() {
    return myAction;
  }

  void addDuplicate(LR1ActionRecord rec) {
    duplicates.add(rec);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    List<LR1Item> items = new ArrayList<>();
    items.add(myItem);
    for (LR1ActionRecord r : duplicates) {
      items.add(r.myItem);
    }
    result.append(items).append(" : ").append(myAction);
    return result.toString();
  }
}