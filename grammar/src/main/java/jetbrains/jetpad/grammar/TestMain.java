package jetbrains.jetpad.grammar;

import jetbrains.jetpad.grammar.lr1.LR1TableGenerator;
import jetbrains.jetpad.grammar.slr.SLRTableGenerator;

public class TestMain {
  public static void main(String[] args) {
    testBookGrammar();
  }

  private static void testFunGrammar() {
    Grammar g = new Grammar();

    NonTerminal e = g.newNonTerminal("E");
    g.newRule(g.getStart(), e);

    Terminal lp = g.newTerminal("(");
    Terminal rp = g.newTerminal(")");
    Terminal colon = g.newTerminal(":");
    Terminal arrow = g.newTerminal("->");
    Terminal id = g.newTerminal("id");


    g.newRule(e, e, e).setPriority(10).setAssociativity(Associativity.LEFT);
    g.newRule(e, e, arrow, e).setPriority(5).setAssociativity(Associativity.LEFT);
//    g.newRule(e, lp, id, colon, e, rp, arrow, e);
//    g.newRule(e, lp, e, rp);
    g.newRule(e, id);


    System.out.println("LR1");

    LR1TableGenerator lr1gen = new LR1TableGenerator(g);
    lr1gen.dumpTable();
  }


  private static void testBookGrammar() {

    Grammar g = new Grammar();

    NonTerminal s = g.newNonTerminal("S");
    NonTerminal e = g.newNonTerminal("E");
    NonTerminal t = g.newNonTerminal("T");

    Terminal min = g.newTerminal("-");
    Terminal n = g.newTerminal("n");
    Terminal lp = g.newTerminal("(");
    Terminal rp = g.newTerminal(")");

    g.newRule(g.getStart(), s);

    g.newRule(s, e);
    g.newRule(e, e, min, t);
    g.newRule(e, t);
    g.newRule(t, n);
    g.newRule(t, lp, e, rp);


    new SLRTableGenerator(g).dumpTable();
  }
}
