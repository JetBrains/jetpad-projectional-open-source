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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ErrorToken extends BaseToken {
  private String myText;

  public ErrorToken(String text) {
    myText = text;
  }

  @Override
  public String text() {
    return myText;
  }

  @Override
  public int hashCode() {
    return myText.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ErrorToken)) return false;

    return ((ErrorToken) obj).myText.equals(myText);
  }

  @Override
  public String toString() {
    return "error: '" + myText + "'";
  }

  @Override
  public Predicate<String> getValidator() {
    return Predicates.alwaysFalse();
  }
}