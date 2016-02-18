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
package jetbrains.jetpad.hybrid.parser;

/**
 * Token which represents an object which technically isn't a normal token but which
 * can't be tokenized further and should be treated by a parser as an already
 * parsed node, i.e. parser should just return a value
 */
public class ValueToken implements Token {
  private Object myValue;
  private ValueCloner myCloner;

  public <ValueT> ValueToken(ValueT val, ValueCloner<ValueT> cloner) {
    myValue = val;
    myCloner = cloner;
  }

  public Object value() {
    return myValue;
  }

  public ValueToken copy() {
    return new ValueToken(myCloner.clone(myValue), myCloner);
  }

  @Override
  public String text() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "(val " + myValue + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ValueToken that = (ValueToken) o;

    return myValue.equals(that.myValue);
  }

  @Override
  public int hashCode() {
    return myValue.hashCode();
  }

  public interface ValueCloner<ValueT> {
    ValueT clone(ValueT val);
  }
}