/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.hybrid.parser;

import java.util.ArrayList;
import java.util.List;

public class ParsingContext {
  private List<Token> myTokens;
  private int myPosition;

  public ParsingContext(List<Token> tokens) {
    myTokens = new ArrayList<Token>(tokens);
  }

  public Token current() {
    if (myPosition > myTokens.size() - 1) return null;
    return myTokens.get(myPosition);
  }

  public void advance() {
    myPosition++;
  }

  public State saveState() {
    return new State();
  }

  public class State {
    private int myPosition;

    private State() {
      myPosition = ParsingContext.this.myPosition;
    }

    public void restore() {
      ParsingContext.this.myPosition = myPosition;
    }
  }
}