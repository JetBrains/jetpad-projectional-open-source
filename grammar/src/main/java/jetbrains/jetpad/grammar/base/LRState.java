package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Associativity;
import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;
import jetbrains.jetpad.grammar.parser.LRParserAction;

import java.util.*;

public class LRState<ItemT extends LRItem<ItemT>> {

  private int myNumber;
  private Set<ItemT> myItems = new LinkedHashSet<>();
  private Map<Symbol, Set<LRActionRecord<ItemT>>> myActionRecords = new HashMap<>();

  private Set<LRTransition<ItemT>> myTransitions = new LinkedHashSet<>();

  LRState(int number, Set<ItemT> items) {
    myNumber = number;
    myItems.addAll(items);
  }

  public String getName() {
    return "S" + myNumber;
  }

  public int getNumber() {
    return myNumber;
  }

  public Set<ItemT> getItems() {
    return Collections.unmodifiableSet(myItems);
  }

  public Set<ItemT> getKernelItems() {
    Set<ItemT> items = new LinkedHashSet<>();
    for (ItemT item : myItems) {
      if (item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  public Set<ItemT> getNonKernelItems() {
    Set<ItemT> items = new LinkedHashSet<>();
    for (ItemT item : myItems) {
      if (!item.isKernel()) {
        items.add(item);
      }
    }
    return Collections.unmodifiableSet(items);
  }

  public Set<LRTransition<ItemT>> getTransitions() {
    return Collections.unmodifiableSet(myTransitions);
  }

  public Set<LRActionRecord<ItemT>> getRecords(Symbol s) {
    Set<LRActionRecord<ItemT>> records = myActionRecords.get(s);
    if (records == null) return Collections.emptySet();
    return Collections.unmodifiableSet(records);
  }

  public Set<LRActionRecord<ItemT>> getMergedRecords(Symbol s) {
    return mergeActions(getRecords(s));
  }

  public boolean hasRecords(Symbol s) {
    return myActionRecords.get(s) != null && !myActionRecords.get(s).isEmpty();
  }

  public boolean hasAmbiguity(Symbol s) {
    Set<LRActionRecord<ItemT>> records = getRecords(s);
    if (records.size() == 1) return false;
    return disambiguate(records) == null;
  }

  public LRActionRecord<ItemT> getRecord(Symbol s) {
    Set<LRActionRecord<ItemT>> records = getRecords(s);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    LRActionRecord<ItemT> result = disambiguate(records);
    if (result == null) {
      throw new IllegalStateException("There's ambiguity");
    }
    return result;
  }

  public LRState<ItemT> getState(Symbol symbol) {
    for (LRTransition<ItemT> t : myTransitions) {
      if (t.getSymbol() == symbol) return t.getTarget();
    }
    return null;
  }

  public void addTransition(LRTransition<ItemT> t) {
    myTransitions.add(t);
  }

  public void addRecord(Symbol s, LRActionRecord<ItemT> rec) {
    if (!myActionRecords.containsKey(s)) {
      myActionRecords.put(s, new HashSet<LRActionRecord<ItemT>>());
    }
    myActionRecords.get(s).add(rec);
  }

  private LRActionRecord<ItemT> disambiguate(Set<LRActionRecord<ItemT>> records) {
    records = mergeActions(records); //todo need a test for this ambiguity (it happens in dot operation between .id and .id(args))
    records = filterByPriority(records);
    if (records.size() == 1) {
      return records.iterator().next();
    }
    LRActionRecord<ItemT> result = disambiguateByAssoc(records);
    if (result != null) return result;
    return null;
  }

  private Set<LRActionRecord<ItemT>> mergeActions(Set<LRActionRecord<ItemT>> records) {
    Set<LRActionRecord<ItemT>> result = new HashSet<>();
    Map<LRParserAction<LRState<ItemT>>, LRActionRecord<ItemT>> actions = new HashMap<>();

    for (LRActionRecord<ItemT> r : records) {
      if (actions.containsKey(r.getAction())) {
        actions.get(r.getAction()).addDuplicate(r);
        continue;
      }
      result.add(r);
      actions.put(r.getAction(), r);
    }

    return result;
  }

  private Set<LRActionRecord<ItemT>> filterByPriority(Set<LRActionRecord<ItemT>> records) {
    Integer highestPriority = null;
    for (LRActionRecord<ItemT> rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (currentPriority == null) return records;
      if (highestPriority == null) {
        highestPriority = currentPriority;
      } else {
        highestPriority = Math.max(highestPriority, currentPriority);
      }
    }

    Set<LRActionRecord<ItemT>> result = new HashSet<>();
    for (LRActionRecord<ItemT> rec : records) {
      Integer currentPriority = rec.getItem().getRule().getPriority();
      if (com.google.common.base.Objects.equal(currentPriority, highestPriority)) {
        result.add(rec);
      }
    }
    return result;
  }

  private LRActionRecord<ItemT> disambiguateByAssoc(Set<LRActionRecord<ItemT>> records) {
    Integer priority = null;
    Rule rule = null;
    for (LRActionRecord<ItemT> rec : records) {
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

    LRActionRecord<ItemT> bestRecord = records.iterator().next();
    for (LRActionRecord<ItemT> rec : records) {
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
    if (!(obj instanceof LRState)) return false;

    LRState<ItemT> otherState = (LRState<ItemT>) obj;
    return myItems.equals(otherState.myItems);
  }

  @Override
  public String toString() {
    return getName() + " : " + getKernelItems()  + " / " + getNonKernelItems();
  }

}
