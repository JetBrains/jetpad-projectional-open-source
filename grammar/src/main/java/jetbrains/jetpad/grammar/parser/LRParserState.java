/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.grammar.parser;

import jetbrains.jetpad.grammar.NonTerminal;
import jetbrains.jetpad.grammar.Terminal;

import java.util.HashMap;
import java.util.Map;

public class LRParserState {
  private String myName;
  private Map<Terminal, LRParserAction<LRParserState>> myActions = new HashMap<>();
  private Map<NonTerminal, LRParserState> myNextStates = new HashMap<>();

  LRParserState(String name) {
    myName = name;
  }

  public LRParserAction<LRParserState> getAction(Terminal terminal) {
    LRParserAction<LRParserState> action = myActions.get(terminal);
    if (action != null) return action;
    return LRParserAction.error();
  }

  public LRParserState getNextState(NonTerminal nonTerminal) {
    LRParserState result = myNextStates.get(nonTerminal);
    if (result == null) {
      throw new IllegalStateException();
    }
    return result;
  }

  public void addAction(Terminal terminal, LRParserAction<LRParserState> action) {
    if (myActions.containsKey(terminal)) {
      throw new IllegalStateException();
    }
    myActions.put(terminal, action);
  }

  public void addNextState(NonTerminal nonTerminal, LRParserState state) {
    if (myNextStates.containsKey(nonTerminal)) {
      throw new IllegalStateException();
    }
    myNextStates.put(nonTerminal, state);
  }

  @Override
  public String toString() {
    return myName;
  }
}