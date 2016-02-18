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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Validators;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.parser.BoolValueToken;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.hybrid.parser.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenCompletionItems {
  private Function<Token, Runnable> myTokenHandler;

  public TokenCompletionItems(Function<Token, Runnable> tokenHandler) {
    myTokenHandler = tokenHandler;
  }

  public CompletionItem forToken(final Token token) {
    return new SimpleCompletionItem(token.toString()) {
      @Override
      public Runnable complete(String text) {
        return myTokenHandler.apply(token);
      }

      @Override
      public String toString() {
        return "Token[" + token + "]";
      }
    };
  }

  public List<CompletionItem> forToken(final Token token, String... matchingTexts) {
    List<CompletionItem> result = new ArrayList<>(Collections.singleton(forToken(token)));
    for (final String match : matchingTexts) {
      result.add(new SimpleCompletionItem(match) {
        @Override
        public Runnable complete(String text) {
          return myTokenHandler.apply(token);
        }

        @Override
        public String toString() {
          return match + "->Token[" + token + "]";
        }
      });
    }
    return result;
  }

  public List<CompletionItem> forTokens(Token ...tokens) {
    List<CompletionItem> result = new ArrayList<>();
    for (Token t : tokens) {
      result.add(forToken(t));
    }
    return result;
  }

  public List<CompletionItem> forBooleans() {
    return forTokens(new BoolValueToken(true), new BoolValueToken(false));
  }

  public CompletionItem forId() {
    return forId(Validators.identifier());
  }

  public CompletionItem forId(final Predicate<String> idPredicate) {
    return new CompletionItem() {
      @Override
      public String visibleText(String text) {
        return "id";
      }

      @Override
      public boolean isStrictMatchPrefix(String text) {
        if ("".equals(text)) return true;
        return isMatch(text);
      }

      @Override
      public boolean isMatch(String text) {
        return idPredicate.apply(text);
      }

      @Override
      public Runnable complete(String text) {
        return myTokenHandler.apply(new IdentifierToken(text));
      }

      @Override
      public boolean isLowMatchPriority() {
        return true;
      }
    };
  }

  public CompletionItem forNumber() {
    return new CompletionItem() {
      @Override
      public String visibleText(String text) {
        return "number";
      }

      @Override
      public boolean isStrictMatchPrefix(String text) {
        if ("".equals(text)) return true;
        return isMatch(text);
      }

      @Override
      public boolean isMatch(String text) {
        return Validators.unsignedInteger().apply(text);
      }

      @Override
      public Runnable complete(String text) {
        int value;
        if (text == null || text.isEmpty()) {
          value = 0;
        } else {
          value = Integer.parseInt(text);
        }
        return myTokenHandler.apply(new IntValueToken(value));
      }

      @Override
      public String toString() {
        return "number completion";
      }
    };
  }
}