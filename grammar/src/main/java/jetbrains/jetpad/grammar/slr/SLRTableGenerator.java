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
package jetbrains.jetpad.grammar.slr;

import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.base.BaseLRTableGenerator;
import jetbrains.jetpad.grammar.base.LRActionRecord;
import jetbrains.jetpad.grammar.base.LRState;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.*;

public class SLRTableGenerator extends BaseLRTableGenerator<SLRItem> {
  public SLRTableGenerator(Grammar grammar) {
    super(grammar);
  }

  @Override
  protected SLRItem initialItem() {
    NonTerminal initial = grammar().getStart();
    return new SLRItem(initial.getFirstRule(), 0);
  }

  @Override
  protected void addFinal(LRState<SLRItem> state, SLRItem item) {
    NonTerminal initial = grammar().getStart();
    final SLRItem finalItem = new SLRItem(initial.getFirstRule(), initial.getFirstRule().getSymbols().size());

    for (Terminal t : item.getRule().getHead().getFollow()) {
      if (t == grammar().getEnd() && finalItem.equals(item)) {
        state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<SLRItem>>accept()));
      } else {
        state.addRecord(t, new LRActionRecord<>(item, LRParserAction.<LRState<SLRItem>>reduce(item.getRule())));
      }
    }
  }

  @Override
  protected Set<SLRItem> closure(SLRItem item) {
    if (item.isFinal()) return Collections.emptySet();
    if (!(item.getNextSymbol() instanceof NonTerminal)) return Collections.emptySet();

    Set<SLRItem> result = new HashSet<>();
    NonTerminal currentNonTerminal = (NonTerminal) item.getNextSymbol();
    for (Rule rule : currentNonTerminal.getRules()) {
      SLRItem newItem = new SLRItem(rule, 0);
      if (!result.contains(newItem)) {
        result.add(newItem);
      }
    }

    return result;
  }
}