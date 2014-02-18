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
package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.parser.LRParserAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LRActionRecord<ItemT extends LRItem<ItemT>> {
  private ItemT myItem;
  private LRParserAction<LRState<ItemT>> myAction;
  private Set<LRActionRecord<ItemT>> duplicates = new HashSet<>();

  public LRActionRecord(ItemT item, LRParserAction<LRState<ItemT>> action) {
    myItem = item;
    myAction = action;
  }

  public ItemT getItem() {
    return myItem;
  }

  public LRParserAction<LRState<ItemT>> getAction() {
    return myAction;
  }

  public void addDuplicate(LRActionRecord<ItemT> rec) {
    duplicates.add(rec);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    List<ItemT> items = new ArrayList<>();
    items.add(myItem);
    for (LRActionRecord<ItemT> r : duplicates) {
      items.add(r.myItem);
    }
    result.append(items).append(" : ").append(myAction);
    return result.toString();
  }

}