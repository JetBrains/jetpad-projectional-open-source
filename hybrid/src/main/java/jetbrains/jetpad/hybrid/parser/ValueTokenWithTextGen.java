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

import com.google.common.base.Function;

public class ValueTokenWithTextGen<ValueT> extends ValueToken {
  private final Function<ValueT, String> myTextGenerator;

  public ValueTokenWithTextGen(ValueT val, ValueCloner<ValueT>  cloner, Function<ValueT, String> textGen) {
    super(val, cloner);
    myTextGenerator = textGen;
  }

  @Override
  public String text() {
    return myTextGenerator.apply((ValueT) value());
  }

  @Override
  public ValueTokenWithTextGen copy() {
    return new ValueTokenWithTextGen<>((ValueT) cloner().clone(value()), cloner(), myTextGenerator);
  }
}
