package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.Grammar;
import jetbrains.jetpad.grammar.NonTerminal;
import jetbrains.jetpad.grammar.Rule;

public class BaseLRTableGenerator<ItemT extends LRItem<ItemT>> {
  private Grammar myGrammar;

  public BaseLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  protected Grammar grammar() {
    return myGrammar;
  }

  protected void checkGrammar() {
    NonTerminal start = myGrammar.getStart();
    if (start.getRules().size() != 1) throw new IllegalArgumentException();
    Rule firstRule = start.getRules().iterator().next();
    if (firstRule.getSymbols().size() != 1) throw new IllegalArgumentException();
    if (!(firstRule.getSymbols().get(0) instanceof NonTerminal)) throw new IllegalArgumentException();
  }
}
