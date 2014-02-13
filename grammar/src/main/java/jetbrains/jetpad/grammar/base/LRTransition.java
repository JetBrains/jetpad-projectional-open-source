package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Symbol;

class LRTransition<ItemT extends LRItem<ItemT>> {
  private LRState<ItemT> myTarget;
  private Symbol mySymbol;

  LRTransition(LRState<ItemT> target, Symbol symbol) {
    myTarget = target;
    mySymbol = symbol;
  }

  LRState<ItemT> getTarget() {
    return myTarget;
  }

  Symbol getSymbol() {
    return mySymbol;
  }

  @Override
  public String toString() {
    return "on " + getSymbol() + " -> " + getTarget().getName();
  }
}
