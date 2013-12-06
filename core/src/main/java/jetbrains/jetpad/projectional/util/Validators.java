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
package jetbrains.jetpad.projectional.util;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

public class Validators {
  private static final Predicate<String> IDENTIFIER = new Predicate<String>() {
    @Override
    public boolean apply(String input) {
      if (input == null) return false;
      if (input.isEmpty()) return false;

      for (int i = 0; i < input.length(); i++) {
        char ch = input.charAt(i);
        if (i == 0 && Character.isDigit(ch)) return false;
        if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_') return false;
      }

      return true;
    }
  };
  private static final Predicate<String> INTEGER = new Predicate<String>() {
    @Override
    public boolean apply(String input) {
      if (Strings.isNullOrEmpty(input)) return false;
      for (int i = 0; i < input.length(); i++) {
        if (!Character.isDigit(input.charAt(i))) return false;
      }
      try {
        Integer.parseInt(input);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  };
  private static final Predicate<String> BOOL = new Predicate<String>() {
    @Override
    public boolean apply(String input) {
      return "true".equals(input) || "false".equals(input);
    }
  };

  public static Predicate<String> identifier() {
    return IDENTIFIER;
  }

  public static Predicate<String> integer() {
    return INTEGER;
  }

  public static Predicate<String> bool() {
    return BOOL;
  }

  public static <T> Predicate<T> equalsTo(final T value) {
    return new Predicate<T>() {
      @Override
      public boolean apply(T input) {
        return Objects.equal(input, value);
      }
    };
  }
}