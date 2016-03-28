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
import jetbrains.jetpad.values.Color;

import javax.annotation.Nullable;

public final class CommentToken extends SimpleToken {

  private final String myPrefix;

  public CommentToken(String prefix, String bodyText) {
    super(prefix + bodyText);
    if (prefix == null) {
      throw new NullPointerException("Null prefix");
    }
    if (bodyText == null) {
      throw new NullPointerException("Null bodyText");
    }
    myPrefix = prefix;
  }

  public String getPrefix() {
    return myPrefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentToken)) return false;
    return text().equals(((CommentToken) o).text());
  }

  @Override
  public int hashCode() {
    return text().hashCode();
  }

  @Override
  public Color getColor() {
    return Color.GRAY;
  }

  @Override
  public Predicate<String> getValidator() {
    return new Predicate<String>() {
      @Override
      public boolean apply(@Nullable String input) {
        return input != null && input.startsWith(myPrefix);
      }
    };
  }
}
