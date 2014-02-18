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
