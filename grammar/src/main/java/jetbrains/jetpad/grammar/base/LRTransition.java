package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Symbol;

public class LRTransition<ItemT extends LRItem<ItemT>> {
  private LRState<ItemT> myTarget;
  private Symbol mySymbol;

  public LRTransition(LRState<ItemT> target, Symbol symbol) {
    myTarget = target;
    mySymbol = symbol;
  }

  public LRState<ItemT> getTarget() {
    return myTarget;
  }

  public Symbol getSymbol() {
    return mySymbol;
  }

  @Override
  public String toString() {
    return "on " + getSymbol() + " -> " + getTarget().getName();
  }
}
