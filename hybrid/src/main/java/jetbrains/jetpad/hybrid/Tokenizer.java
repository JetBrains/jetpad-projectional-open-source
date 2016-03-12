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

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.hybrid.parser.ErrorToken;
import jetbrains.jetpad.hybrid.parser.Token;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

class Tokenizer {
  private final TokenCompletion mySpec;

  Tokenizer(TokenCompletion spec) {
    mySpec = spec;
  }

  List<Token> tokenize(String input) {
    TokensCollector tc = new TokensCollector();
    for (char currentChar : input.toCharArray()) {
      tc.append(currentChar);
    }
    tc.collectLastToken();
    return tc.tokens;
  }

  private class TokensCollector {
    private final TextMatcher textMatcher = new TextMatcher();
    private final StringBuilder currentTokenCandidate = new StringBuilder();
    private TextMatchResult currentMatchResult = pending("");
    private final List<Token> tokens = new ArrayList<>();

    private void append(char ch) {
      if (CharMatcher.whitespace().matches(ch)) {
        if (currentTokenCandidate.length() == 0) {
           return;
        }
        if (currentMatchResult.noWayToComplete) {
          tokens.add(currentMatchResult.pendingToken);
          clear();
          return;
        }
      }

      TextMatchResult prevMatchResult = currentMatchResult;
      currentTokenCandidate.append(ch);
      updateMatchResult();
      if (currentMatchResult.noWayToComplete && prevMatchResult.matchesSingleToken) {
        tokens.add(prevMatchResult.singleMatchedToken);
        clear();
        append(ch);
      }
    }

    private void collectLastToken() {
      if (currentMatchResult.matchesSingleToken) {
        tokens.add(currentMatchResult.singleMatchedToken);
      } else if (currentMatchResult.isPendingTokenNonEmpty()) {
        tokens.add(currentMatchResult.pendingToken);
      }
    }

    private void clear() {
      currentTokenCandidate.setLength(0);
      currentMatchResult = pending("");
    }

    private void updateMatchResult() {
      currentMatchResult = textMatcher.match(currentTokenCandidate.toString());
    }
  }

  private class TextMatcher {
    private final Value<Token> tokenHolder = new Value<>();
    private final CompletionItems completionItems = new CompletionItems(
        mySpec.getTokenCompletion(new Function<Token, Runnable>() {
          @Nullable
          @Override
          public Runnable apply(@Nullable final Token token) {
            return new Runnable() {
              @Override
              public void run() {
                tokenHolder.set(token);
              }
            };
          }
        }).get(CompletionParameters.EMPTY));

    private TextMatchResult match(String text) {
      List<CompletionItem> basicMatches = completionItems.matches(text);
      if (basicMatches.size() == 1) {
        basicMatches.iterator().next().complete(text).run();
        return singleMatched(tokenHolder.get());
      } else {
        boolean noWayToComplete = basicMatches.isEmpty() && completionItems.prefixedBy(text).isEmpty();
        if (noWayToComplete) {
          return error(text);
        } else {
          return pending(text);
        }
      }
    }
  }

  private static TextMatchResult singleMatched(Token token) {
    return new TextMatchResult(true, token, false, null);
  }

  private static TextMatchResult error(String text) {
    return new TextMatchResult(false, null, true, new ErrorToken(text));
  }

  private static TextMatchResult pending(String text) {
    return new TextMatchResult(false, null, false, new ErrorToken(text));
  }

  private static class TextMatchResult {
    private final boolean matchesSingleToken;
    private final Token singleMatchedToken;
    private final boolean noWayToComplete;
    private final ErrorToken pendingToken;
    private TextMatchResult(boolean matchesSingleToken, Token singleMatchedToken, boolean noWayToComplete, ErrorToken pendingToken) {
      this.matchesSingleToken = matchesSingleToken;
      this.singleMatchedToken = singleMatchedToken;
      this.noWayToComplete = noWayToComplete;
      this.pendingToken = pendingToken;
    }
    private boolean isPendingTokenNonEmpty() {
      return pendingToken != null && !pendingToken.text().isEmpty();
    }
  }
}
