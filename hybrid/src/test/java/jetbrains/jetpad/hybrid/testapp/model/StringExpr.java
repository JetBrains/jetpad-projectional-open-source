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
package jetbrains.jetpad.hybrid.testapp.model;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadOnlyProperty;
import jetbrains.jetpad.model.property.ValueProperty;

public class StringExpr extends Expr {
  public final ReadOnlyProperty<String> quote;
  public final Property<String> body = new ValueProperty<>("");

  public StringExpr(String quote) {
    this.quote = new ReadOnlyProperty<>(new ValueProperty<>(quote));
  }

  public StringExpr(String quote, String body) {
    this(quote);
    this.body.set(body);
  }

  public StringExpr(StringExpr toCopy) {
    this(toCopy.quote.get());
    this.body.set(toCopy.body.get());
  }

  @Override
  public String toString() {
    return quote.get() + body.get() + quote.get();
  }
}
