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

import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.hybrid.TokensUtil.comment;

public class SimpleHybridEditorExternalTokensChangeTest extends BaseHybridEditorTest<SimpleExprContainer, SimpleExprContainerMapper> {
  @Override
  protected SimpleExprContainer createContainer() {
    return new SimpleExprContainer();
  }

  @Override
  protected SimpleExprContainerMapper createMapper() {
    return new SimpleExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(SimpleExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }


  @Before
  public void setup() {
    initEditor();
  }

  @Test
  public void addMakeCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
    container.sourceTokens.add(2, Tokens.RP);
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("test"));
  }

  @Test
  public void addMakeIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.add(2, Tokens.RP);
    assertTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, comment("test"));
  }

  @Test
  public void removeMakeCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, comment("test"));
    container.sourceTokens.remove(3);
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("test"));
  }

  @Test
  public void removeMakeIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.remove(2);
    assertTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
  }

  @Test
  public void addCommentToCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP);
    container.sourceTokens.add(comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("test"));
  }

  @Test
  public void addCommentToIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP);
    container.sourceTokens.add(comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
  }

  @Test
  public void removeCommentFromCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.remove(3);
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void removeCommentFromIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
    container.sourceTokens.remove(2);
    assertTokens(new IdentifierToken("x"), Tokens.LP);
  }

  @Test
  public void replaceCommentInCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.set(3, comment("retest"));
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("retest"));
  }

  @Test
  public void replaceCommentInIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
    container.sourceTokens.set(2, comment("retest"));
    assertTokens(new IdentifierToken("x"), Tokens.LP, comment("retest"));
  }

  @Test
  public void replaceCommentWithAnotherCorrectToCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.set(3, Tokens.FACTORIAL);
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, Tokens.FACTORIAL);
  }

  @Test
  public void replaceCommentWithAnotherCorrectToIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, comment("test"));
    container.sourceTokens.set(3, Tokens.RP);
    assertTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP);
  }

  @Test
  public void replaceCommentWithAnotherIncorrectToCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
    container.sourceTokens.set(2, Tokens.RP);
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void replaceCommentWithAnotherIncorrectToIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, comment("test"));
    container.sourceTokens.set(4, Tokens.FACTORIAL);
    assertTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, Tokens.FACTORIAL);
  }

  @Test
  public void replaceWithCommentCorrectToCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.FACTORIAL);
    container.sourceTokens.set(3, comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("test"));
  }

  @Test
  public void replaceWithCommentCorrectToIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP);
    container.sourceTokens.set(2, comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP, comment("test"));
  }

  @Test
  public void replaceWithCommentIncorrectToCorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP);
    container.sourceTokens.set(3, comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP, comment("test"));
  }

  @Test
  public void replaceWithCommentIncorrectToIncorrect() {
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, Tokens.FACTORIAL);
    container.sourceTokens.set(4, comment("test"));
    assertTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP, Tokens.RP, comment("test"));
  }

  private void assertTokens(Token... tokens) {
    TokensUtil.assertTokensEqual(Arrays.asList(tokens), sync.tokens());
  }

  private void setTokens(Token... tokens) {
    sync.setTokens(Arrays.asList(tokens));
  }
}
