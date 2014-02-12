package jetbrains.jetpad.grammar;

import jetbrains.jetpad.grammar.slr.SLRTableGenerator;

public class Test {
  public static void main(String[] args) {
    Grammar g = new Grammar();

    NonTerminal e = g.newNonTerminal("E");
    g.newRule(g.getStart(), e);

    Terminal lp = g.newTerminal("(");
    Terminal rp = g.newTerminal(")");
    Terminal colon = g.newTerminal(":");
    Terminal arrow = g.newTerminal("\u2192");
    Terminal id = g.newTerminal("id");


    g.newRule(e, e, e).setPriority(10);
    g.newRule(e, e, arrow, e).setPriority(5);
//    g.newRule(e, lp, id, colon, e, rp, arrow, e);
//    g.newRule(e, lp, e, rp);
    g.newRule(e, id);


    SLRTableGenerator gen = new SLRTableGenerator(g);

    gen.dumpTable();

    gen.generateTable();


  }
}
