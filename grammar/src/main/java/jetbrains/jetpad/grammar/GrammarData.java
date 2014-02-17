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

import java.util.*;

class GrammarData {
  private Grammar myGrammar;

  private Set<NonTerminal> myNullable = new HashSet<NonTerminal>();
  private Map<NonTerminal, Set<Terminal>> myFirst = new LinkedHashMap<NonTerminal, Set<Terminal>>();
  private Map<NonTerminal, Set<Terminal>> myFollow = new LinkedHashMap<NonTerminal, Set<Terminal>>();

  GrammarData(Grammar grammar) {
    myGrammar = grammar;

    calculateCanBeEmpty();
    calculateFirst();
    calculateFollow();
  }

  boolean isNullable(NonTerminal nt) {
    return myNullable.contains(nt);
  }

  Set<Terminal> getFirst(NonTerminal nt) {
    return Collections.unmodifiableSet(myFirst.get(nt));
  }

  Set<Terminal> getFollow(NonTerminal nt) {
    return Collections.unmodifiableSet(myFollow.get(nt));
  }

  private void calculateCanBeEmpty() {
    boolean hasChanges = true;

    while (hasChanges) {
      hasChanges = false;
      for (NonTerminal nt : myGrammar.getNonTerminals()) {
        if (myNullable.contains(nt)) continue;

        for (Rule rule : nt.getRules()) {
          boolean canBeEmpty = true;
          for (Symbol s : rule.getSymbols()) {
            if (s instanceof Terminal) {
              canBeEmpty = false;
              break;
            }

            if (s instanceof NonTerminal && !myNullable.contains((NonTerminal) s)) {
              canBeEmpty = false;
              break;
            }
          }

          if (canBeEmpty) {
            myNullable.add(nt);
            hasChanges = true;
          }
        }
      }
    }
  }

  private void calculateFirst() {
    for (NonTerminal nt : myGrammar.getNonTerminals()) {
      myFirst.put(nt, new LinkedHashSet<Terminal>());
    }

    boolean hasChanges = true;

    while (hasChanges) {
      hasChanges = false;
      for (NonTerminal nt : myGrammar.getNonTerminals()) {
        Set<Terminal> first = myFirst.get(nt);
        for (Rule rule : nt.getRules()) {
          for (Symbol s : rule.getSymbols()) {
            if (s instanceof Terminal) {
              Terminal term = (Terminal) s;
              hasChanges |= first.add(term);
              break;
            }

            if (s instanceof NonTerminal) {
              NonTerminal nonTerminal = (NonTerminal) s;
              hasChanges |= first.addAll(myFirst.get(nonTerminal));
              if (!myNullable.contains(nonTerminal)) {
                break;
              }
            }
          }
        }
      }
    }
  }

  private void calculateFollow() {
    for (NonTerminal nt : myGrammar.getNonTerminals()) {
      myFollow.put(nt, new LinkedHashSet<Terminal>());
    }

    boolean hasChanges = true;
    myFollow.get(myGrammar.getStart()).add(myGrammar.getEnd());

    while (hasChanges) {
      hasChanges = false;

      for (NonTerminal nt : myGrammar.getNonTerminals()) {
        for (Rule rule : nt.getRules()) {
          List<Symbol> symbols = rule.getSymbols();
          for (int i = 0; i < symbols.size(); i++) {
            Symbol s = symbols.get(i);
            for (int d = 1; d < symbols.size() - i; d++) {
              Symbol next = symbols.get(i + d);
              if (s instanceof NonTerminal && next != null) {
                NonTerminal nonTerminal = (NonTerminal) s;
                Set<Terminal> follow = myFollow.get(nonTerminal);
                if (next instanceof Terminal) {
                  hasChanges |= follow.add((Terminal) next);
                  break;
                } else if (next instanceof NonTerminal) {
                  hasChanges |= follow.addAll(myFirst.get((NonTerminal) next));
                  if (!myNullable.contains((NonTerminal) next)) {
                    break;
                  }
                }
              }
            }
          }

          for (int i = symbols.size() - 1; i >= 0; i--) {
            Symbol s = symbols.get(i);

            if (s instanceof Terminal) {
              break;
            }

            if (s instanceof NonTerminal) {
              NonTerminal nonTerminal = (NonTerminal) s;
              hasChanges |= myFollow.get(nonTerminal).addAll(myFollow.get(nt));
              if (!myNullable.contains(nonTerminal)) break;
            }
          }
        }
      }
    }
  }
}