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

public final class Grammar {
  private Terminal myEnd;
  private NonTerminal myStart;
  private Map<String, Symbol> mySymbols = new HashMap<>();
  private Set<Terminal> myTerminals = new LinkedHashSet<>();
  private Set<NonTerminal> myNonTerminals = new LinkedHashSet<>();
  private Set<Rule> myRules = new LinkedHashSet<>();

  private GrammarData myGrammarData;

  public Grammar() {
    myEnd = newTerminal("$");
    myStart = newNonTerminal("Start");
  }

  public NonTerminal getStart() {
    return myStart;
  }

  public Terminal getEnd() {
    return myEnd;
  }

  public Terminal newTerminal(String name) {
    checkName(name);

    try {
      Terminal result = new Terminal(this, name);
      myTerminals.add(result);
      mySymbols.put(name, result);
      return result;
    } finally {
      invalidateGrammarData();
    }
  }

  public NonTerminal newNonTerminal(String name) {
    checkName(name);

    NonTerminal result = new NonTerminal(this, name);
    myNonTerminals.add(result);
    mySymbols.put(name, result);

    invalidateGrammarData();

    return result;
  }

  public String uniqueName(String prefix) {
    return uniqueName(prefix, true);
  }

  public String uniqueName(String prefix, boolean forceNumber) {
    if (!forceNumber && !mySymbols.containsKey(prefix)) {
      return prefix;
    }

    int i = 0;
    while (true) {
      String name = prefix + i;
      if (!mySymbols.containsKey(name)) return name;
      i++;
    }
  }

  private void checkName(String name) {
    if (mySymbols.containsKey(name)) throw new IllegalArgumentException("Duplicate name");
  }

  public Rule newRule(NonTerminal head, Symbol... symbols) {
    try {
      if (head.getGrammar() != this) throw new IllegalArgumentException();
      for (Symbol s : symbols) {
        if (s.getGrammar() != this) throw new IllegalArgumentException();
      }

      Rule result = new Rule(this, head, symbols);
      myRules.add(result);
      head.addRule(result);
      return result;
    } finally {
      invalidateGrammarData();
    }
  }

  public Set<Terminal> getTerminals() {
    return Collections.unmodifiableSet(myTerminals);
  }

  public Set<NonTerminal> getNonTerminals() {
    return Collections.unmodifiableSet(myNonTerminals);
  }

  public Set<Symbol> getSymbols() {
    Set<Symbol> symbols = new LinkedHashSet<>();
    symbols.addAll(myNonTerminals);
    symbols.addAll(myTerminals);
    return Collections.unmodifiableSet(symbols);
  }

  public Set<Rule> getRules() {
    return Collections.unmodifiableSet(myRules);
  }

  public Set<Terminal> first(List<Symbol> string) {
    Set<Terminal> result = new HashSet<>();
    for (Symbol s : string) {
      if (s instanceof Terminal) {
        result.add((Terminal) s);
        return result;
      } else if (s instanceof NonTerminal) {
        NonTerminal nt = (NonTerminal) s;
        result.addAll(nt.getFirst());
        if (!nt.isNullable()) return result;
      }
    }
    return result;
  }

  private void invalidateGrammarData() {
    myGrammarData = null;
  }

  GrammarData getGrammarData() {
    if (myGrammarData == null) {
      myGrammarData = new GrammarData(this);
    }

    return myGrammarData;
  }
}