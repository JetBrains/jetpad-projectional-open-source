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
package jetbrains.jetpad.grammar.lr;

import jetbrains.jetpad.grammar.Rule;

public abstract class LRAction {
  public static LRAction shift(LRState state) {
    return new Shift(state);
  }

  public static LRAction reduce(Rule rule) {
    return new Reduce(rule);
  }

  public static LRAction accept() {
    return new Accept();
  }

  public static LRAction error() {
    return new Error();
  }

  private LRAction() {
  }

  public static class Shift extends LRAction {
    private LRState myState;

    private Shift(LRState state) {
      this.myState = state;
    }

    public LRState getState() {
      return myState;
    }

    @Override
    public String toString() {
      return "shift " + myState;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Shift)) return false;

      Shift otherShift = (Shift) obj;
      return otherShift.myState == myState;
    }

    @Override
    public int hashCode() {
      return myState.hashCode();
    }
  }

  public static class Reduce extends LRAction {
    private Rule myRule;

    private Reduce(Rule rule) {
      myRule = rule;
    }

    public Rule getRule() {
      return myRule;
    }

    @Override
    public String toString() {
      return "reduce " + myRule;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Reduce)) return false;
      Reduce otherReduce = (Reduce) obj;
      return otherReduce.myRule == myRule;
    }

    @Override
    public int hashCode() {
      return myRule.hashCode();
    }
  }

  public static class Accept extends LRAction {
    private Accept() {
    }

    @Override
    public String toString() {
      return "accept";
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Accept;
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }
  }

  public static class Error extends LRAction {
    private Error() {
    }

    @Override
    public String toString() {
      return "error";
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Error;
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }
  }
}