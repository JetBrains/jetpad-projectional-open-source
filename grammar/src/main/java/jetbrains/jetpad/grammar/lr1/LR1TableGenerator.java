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
package jetbrains.jetpad.grammar.lr1;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.base.*;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.singleton;

public class LR1TableGenerator extends BaseLRTableGenerator<LR1Item> {
  public LR1TableGenerator(Grammar grammar) {
    super(grammar);
  }

  @Override
  protected LR1Item initialItem() {
    NonTerminal initial = grammar().getStart();
    return new LR1Item(initial.getFirstRule(), 0, grammar().getEnd());
  }

  @Override
  protected void addFinal(LRState<LR1Item> state, LR1Item item) {
    NonTerminal initial = grammar().getStart();
    Rule firstRule = initial.getFirstRule();
    final LR1Item finalItem = new LR1Item(firstRule, firstRule.getSymbols().size(), grammar().getEnd());

    Terminal t = item.getLookAhead();
    if (t == grammar().getEnd() && finalItem.equals(item)) {
      state.addRecord(t, new LRActionRecord<LR1Item>(item, LRParserAction.<LRState<LR1Item>>accept()));
    } else {
      state.addRecord(t, new LRActionRecord<LR1Item>(item, LRParserAction.<LRState<LR1Item>>reduce(item.getRule())));
    }
  }

  @Override
  protected boolean closure(Set<LR1Item> result, LR1Item item) {
    if (item.isFinal()) return false;
    if (!(item.getNextSymbol() instanceof NonTerminal)) return false;

    NonTerminal currentNonTerminal = (NonTerminal) item.getNextSymbol();
    List<Symbol> rightPart = item.getRule().getSymbols();
    Set<Terminal> first = first(rightPart.subList(item.getIndex() + 1, rightPart.size()), item.getLookAhead());
    boolean changed = false;
    for (Rule rule : currentNonTerminal.getRules()) {
      for (Terminal t : first) {
        LR1Item newItem = new LR1Item(rule, 0, t);
        if (result.add(newItem)) {
          changed = true;
        }
      }
    }
    return changed;
  }

  private Set<Terminal> first(List<Symbol> symbols, Terminal lookAhead) {
    Set<Terminal> result = new HashSet<Terminal>();
    for (Symbol s : symbols) {
      if (s instanceof Terminal) {
        result.add((Terminal) s);
        return result;
      } else if (s instanceof NonTerminal) {
        NonTerminal nt = (NonTerminal) s;
        result.addAll(nt.getFirst());
        if (!nt.isNullable()) return result;
      }
    }
    result.add(lookAhead);
    return result;
  }
}