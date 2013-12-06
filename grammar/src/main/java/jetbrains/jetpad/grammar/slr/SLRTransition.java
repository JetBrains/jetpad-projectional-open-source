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
package jetbrains.jetpad.grammar.slr;

import jetbrains.jetpad.grammar.Symbol;

public class SLRTransition {
  private SLRState myTarget;
  private Symbol mySymbol;

  SLRTransition(SLRState target, Symbol symbol) {
    myTarget = target;
    mySymbol = symbol;
  }

  public SLRState getTarget() {
    return myTarget;
  }

  public Symbol getSymbol() {
    return mySymbol;
  }

  @Override
  public String toString() {
    return "on " + getSymbol() + " -> " + getTarget().getName();
  }
}