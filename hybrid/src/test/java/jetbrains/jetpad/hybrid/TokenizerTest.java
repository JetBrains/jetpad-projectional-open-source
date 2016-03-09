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

import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static jetbrains.jetpad.hybrid.TokensUtil.*;
import static jetbrains.jetpad.hybrid.testapp.mapper.Tokens.*;
import static org.junit.Assert.assertTrue;

public class TokenizerTest {
  private Tokenizer<Expr> tokenizer = new Tokenizer<>(new ExprHybridEditorSpec());

  @Test
  public void oneToken() {
    List<Token> tokens = tokenizer.tokenize("!");
    assertTokensEqual(of(FACTORIAL), tokens);
  }

  @Test
  public void emptyInput() {
    List<Token> tokens = tokenizer.tokenize("");
    assertTrue(tokens.isEmpty());
  }

  @Test
  public void blankInput() {
    List<Token> tokens = tokenizer.tokenize(" \t ");
    assertTrue(tokens.isEmpty());
  }

  @Test
  public void simpleExpression() {
    List<Token> tokens = tokenizer.tokenize("1+2");
    assertTokensEqual(of(integer(1), PLUS, integer(2)), tokens);
  }

  @Test
  public void simpleExpressionWithWhitespaces() {
    List<Token> tokens = tokenizer.tokenize("  1\t * 2");
    assertTokensEqual(of(integer(1), MUL, integer(2)), tokens);
  }

  @Test
  public void incrementAndPlus() {
    List<Token> tokens = tokenizer.tokenize("+++");
    assertTokensEqual(of(INCREMENT, PLUS), tokens);
  }

  @Test
  public void valueTokens() {
    List<Token> tokens = tokenizer.tokenize("value * aaaa + posValue");
    assertTokensEqual(of(value(), MUL, complex(), PLUS, pos()), tokens);
  }

  @Test
  public void emptyStrings() {
    List<Token> tokens = tokenizer.tokenize("\"\"\"\" '' ''");
    assertTokensEqual(of(doubleQtd(""), doubleQtd(""), singleQtd(""), singleQtd("")), tokens);
  }

  @Test
  public void stringLiterals() {
    final String strBody = "These items must not be tokenized: 10 + 17! * id.value";
    List<Token> tokens = tokenizer.tokenize("id + \"" + strBody + "\"'" + strBody + "'");
    assertTokensEqual(of(ID, PLUS, doubleQtd(strBody), singleQtd(strBody)), tokens);
  }

  @Test
  public void nestedQuotesOfDifferentKind() {
    List<Token> tokens = tokenizer.tokenize("\"'\"'\"'");
    assertTokensEqual(of(doubleQtd("'"), singleQtd("\"")), tokens);
  }

  @Test
  public void longCorrectExpression() {
    List<Token> tokens = tokenizer.tokenize("\t( 10) * 5! + id.value ++ + '\"text 1\"' + \"text 2\"");
    assertTokensEqual(of(LP, integer(10), RP, MUL, integer(5), FACTORIAL, PLUS, ID, DOT, value(), INCREMENT, PLUS,
        singleQtd("\"text 1\""), PLUS, doubleQtd("text 2")),
        tokens);
  }

  @Test
  public void oneIncorrect() {
    List<Token> tokens = tokenizer.tokenize("\tbad  ");
    assertTokensEqual(of(error("bad")), tokens);
  }

  @Test
  public void severalIncorrect() {
    List<Token> tokens = tokenizer.tokenize(" bad \tinput");
    assertTokensEqual(of(error("bad"), error("input")), tokens);
  }

  @Test
  public void correctAndIncorrect() {
    List<Token> tokens = tokenizer.tokenize("value bad + 7 ^ 5");
    assertTokensEqual(of(value(), error("bad"), PLUS, integer(7), error("^"), integer(5)), tokens);
  }

  @Test
  public void recoverOnlyAfterSpace() {
    List<Token> tokens = tokenizer.tokenize("bad+ bad +");
    assertTokensEqual(of(error("bad+"), error("bad"), PLUS), tokens);
  }
}
