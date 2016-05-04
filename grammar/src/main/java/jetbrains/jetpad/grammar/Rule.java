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
import java.util.Collections;
import java.util.List;

/**
 * Grammar rule. It has the following form:
 *  NT = s1 ... sN
 *
 *  If there're ambiguities during grammar generation, they are resolved via associativity and priorirty.
 *
 *  You can specify how the resulting AST is being via {@link RuleHandler}
 */
public class Rule {
  private NonTerminal myHead;
  private List<Symbol> mySymbols;
  private Grammar myGrammar;

  private Associativity myAssociativity;
  private Integer myPriority;
  private RuleHandler myHandler;

  Rule(Grammar grammar, NonTerminal head, Symbol... symbols) {
    myGrammar = grammar;
    myHead = head;
    mySymbols = new ArrayList<>(Arrays.asList(symbols));
  }

  public NonTerminal getHead() {
    return myHead;
  }

  public List<Symbol> getSymbols() {
    return Collections.unmodifiableList(mySymbols);
  }

  public Grammar getGrammar() {
    return myGrammar;
  }

  public Associativity getAssociativity() {
    return myAssociativity;
  }

  public Rule setAssociativity(Associativity associativity) {
    myAssociativity = associativity;
    return this;
  }

  public Integer getPriority() {
    return myPriority;
  }

  public Rule setPriority(Integer priority) {
    myPriority = priority;
    return this;
  }

  public RuleHandler getHandler() {
    return myHandler;
  }

  public Rule setHandler(RuleHandler handler) {
    myHandler = handler;
    return this;
  }

  @Override
  public String toString() {
    String arrow = "\u2192";
    String epsilon = "\u03B5" ;

    StringBuilder result = new StringBuilder();
    result.append(myHead).append(" ").append(arrow);
    if (mySymbols.isEmpty()) {
      result.append(" ").append(epsilon);
    } else {
      for (Symbol s : mySymbols) {
        result.append(" ").append(s);
      }
    }
    return result.toString();
  }
}