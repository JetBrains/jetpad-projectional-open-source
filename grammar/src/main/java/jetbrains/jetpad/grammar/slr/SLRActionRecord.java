package jetbrains.jetpad.grammar.slr;

import jetbrains.jetpad.grammar.lr.LRAction;
import jetbrains.jetpad.grammar.lr.LRState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SLRActionRecord {
  private SLRItem myItem;
  private LRAction<LRState> myAction;
  private Set<SLRActionRecord> duplicates = new HashSet<>();

  SLRActionRecord(SLRItem item, LRAction<LRState> action) {
    this.myItem = item;
    this.myAction = action;
  }

  SLRItem getItem() {
    return myItem;
  }

  LRAction<LRState> getAction() {
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
