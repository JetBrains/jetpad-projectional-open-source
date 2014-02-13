package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Rule;
import jetbrains.jetpad.grammar.Symbol;

public abstract interface LRItem<ItemT extends LRItem<ItemT>> {
  Rule getRule();
  int getIndex();
  boolean isKernel();
  boolean isInitial();
  boolean isFinal();
  Symbol getNextSymbol();
  ItemT getNextItem();
}
