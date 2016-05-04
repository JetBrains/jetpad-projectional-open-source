/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrammarSugar {
  /**
   * NT = S1 S2 ... SN
   */
  public static NonTerminal seq(Symbol... symbols) {
    if (symbols.length == 0) {
      throw new IllegalArgumentException();
    }
    Grammar g = symbols[0].getGrammar();
    NonTerminal seq = g.newNonTerminal(g.uniqueName("seq_"));
    g.newRule(seq, symbols);
    return seq;
  }

  /**
   * NT = S?
   */
  public static NonTerminal optional(Symbol symbol) {
    Grammar g = symbol.getGrammar();
    NonTerminal opt = g.newNonTerminal(g.uniqueName("opt_"));
    g.newRule(opt);
    g.newRule(opt, symbol);
    return opt;
  }

  /**
   * NT = S+
   */
  public static NonTerminal plus(Symbol symbol) {
    Grammar g = symbol.getGrammar();
    NonTerminal plus = g.newNonTerminal(g.uniqueName("plus_"));
    g.newRule(plus, symbol, star(symbol)).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        Object head = ctx.get(0);
        PersistentList list = (PersistentList) ctx.get(1);
        return PersistentList.cons(head, list);
      }
    });
    return plus;
  }

  /**
   * NT = S*
   */
  public static NonTerminal star(Symbol symbol) {
    return star(symbol, new StarRulesUpdater() {
      @Override
      public void updateRules(Rule emptyCase, Rule stepCase) {
      }
    });
  }

  public static NonTerminal star(Symbol symbol, StarRulesUpdater updater) {
    Grammar g = symbol.getGrammar();

    NonTerminal star = g.newNonTerminal(g.uniqueName("star_"));
    Rule emptyCase = g.newRule(star).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        return PersistentList.nil();
      }
    });
    Rule stepCase = g.newRule(star, symbol, star).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        Object first = ctx.get(0);
        PersistentList list = (PersistentList) ctx.get(1);
        return PersistentList.cons(first, list);
      }
    });

    updater.updateRules(emptyCase, stepCase);

    return star;
  }

  /**
   * NT = first
   * NT = S1
   * ...
   * NT = SN
   */
  public static NonTerminal oneOf(Symbol first, Symbol... symbols) {
    OneOfRuleUpdater updater = new OneOfRuleUpdater() {
      @Override
      public void updateRule(Rule rule) {
      }
    };
    return oneOf(updater, first, symbols);
  }

  public static NonTerminal oneOf(OneOfRuleUpdater updater, Symbol first, Symbol... symbols) {
    Grammar g = first.getGrammar();
    NonTerminal oneOf = g.newNonTerminal(g.uniqueName("oneOf_"));
    Rule rule = g.newRule(oneOf, first);
    updater.updateRule(rule);
    for (Symbol s : symbols) {
      rule = g.newRule(oneOf, s);
      updater.updateRule(rule);
    }
    return oneOf;
  }


  /**
   * NT = ((ITEM SEP)* ITEM)?
   */
  public static NonTerminal separated(Symbol item, Symbol separator) {
    return separated(item, separator, false);
  }

  public static NonTerminal separated(Symbol item, Symbol separator, boolean atLeastOne) {
    Grammar g = item.getGrammar();

    NonTerminal sepSeq = g.newNonTerminal(g.uniqueName("separated1_"));
    g.newRule(sepSeq, item, star(seq(separator, item))).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        Object firstItem = ctx.get(0);
        List<List> listOfLists = (List<List>) ctx.get(1);
        List result = new ArrayList();
        result.add(firstItem);
        for (List l : listOfLists) {
          result.add(l.get(1));
        }
        return result;
      }
    });

    if (atLeastOne) {
      return sepSeq;
    } else {
      NonTerminal separated = g.newNonTerminal(g.uniqueName("separated_"));
      g.newRule(separated, optional(sepSeq)).setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          List list = (List) ctx.get(0);
          if (list.isEmpty()) {
            return PersistentList.nil();
          } else {
            return list.get(0);
          }
        }
      });
      return separated;
    }
  }

  /**
   * NT = (item separator)*
   */
  public static NonTerminal terminated(Symbol item, Symbol separator) {
    return terminated(item, separator, false);
  }

  public static NonTerminal terminated(Symbol item, Symbol separator, boolean trailingOptional) {
    return terminated(item, separator, trailingOptional, false);
  }

  public static NonTerminal terminated(Symbol item, Symbol separator, boolean trailingOptional, boolean emptyStart) {
    return terminated(item, separator, separator, trailingOptional, emptyStart);
  }

  public static NonTerminal terminated(Symbol item, Symbol separator, Symbol terminator, boolean trailingOptional, boolean emptyStart) {
    Grammar g = item.getGrammar();
    NonTerminal sepSeq = g.newNonTerminal(g.uniqueName("separatedEndSeq_"));
    NonTerminal sepEnd = g.newNonTerminal(g.uniqueName("separatedEnd_"));
    if (emptyStart) {
      g.newRule(sepSeq).setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return PersistentList.nil();
        }
      });
    } else {
      g.newRule(sepSeq, item).setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return PersistentList.cons(ctx.get(0), PersistentList.nil());
        }
      });
    }
    g.newRule(sepSeq, sepSeq, separator, item).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        Object last = ctx.get(2);
        PersistentList list = (PersistentList) ctx.get(0);
        return PersistentList.cons(last, list);
      }
    });
    if (trailingOptional) {
      g.newRule(sepEnd, sepSeq, optional(terminator)).setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return Arrays.asList(PersistentList.reversed((PersistentList) ctx.get(0)), ctx.get(1));
        }
      });
    } else {
      g.newRule(sepEnd, sepSeq, terminator).setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return PersistentList.reversed((PersistentList) ctx.get(0));
        }
      });
    }
    return sepEnd;
  }

  public interface StarRulesUpdater {
    void updateRules(Rule emptyCase, Rule stepCase);
  }

  public interface OneOfRuleUpdater {
    void updateRule(Rule rule);
  }
}