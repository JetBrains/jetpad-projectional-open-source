/*
 * Copyright 2012-2014 JetBrains s.r.o
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
import java.util.List;

public class GrammarSugar {
  public static NonTerminal seq(Symbol... symbols) {
    if (symbols.length == 0) throw new IllegalArgumentException();
    Grammar g = symbols[0].getGrammar();
    NonTerminal seq = g.newNonTerminal(g.uniqueName("seq_"));
    g.newRule(seq, symbols);
    return seq;
  }

  public static NonTerminal optional(Symbol symbol) {
    Grammar g = symbol.getGrammar();
    NonTerminal opt = g.newNonTerminal(g.uniqueName("opt_"));
    g.newRule(opt);
    g.newRule(opt, symbol);
    return opt;
  }

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

  public static NonTerminal star(Symbol symbol) {
    Grammar g = symbol.getGrammar();

    NonTerminal star = g.newNonTerminal(g.uniqueName("star_"));
    g.newRule(star).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        return PersistentList.nil();
      }
    });
    g.newRule(star, symbol, star).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        Object first = ctx.get(0);
        PersistentList list = (PersistentList) ctx.get(1);
        return PersistentList.cons(first, list);
      }
    });

    return star;
  }

  public static NonTerminal oneOf(Symbol first, Symbol... symbols) {
    Grammar g = first.getGrammar();
    NonTerminal oneOf = g.newNonTerminal(g.uniqueName("oneOf_"));
    g.newRule(oneOf, first);
    for (Symbol s : symbols) {
      g.newRule(oneOf, s);
    }
    return oneOf;
  }

  public static NonTerminal separated(Symbol item, Symbol separator) {
    Grammar g = item.getGrammar();
    NonTerminal separated = g.newNonTerminal(g.uniqueName("separated_"));
    NonTerminal sepSeq = g.newNonTerminal(g.uniqueName("separatedSeq_"));
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