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

import jetbrains.jetpad.hybrid.parser.TerminatorToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.NumberExpr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TokenListEditorTest {
  private static List<Token> simpleTokenList() {
    return Arrays.asList(new IntValueToken(0), Tokens.PLUS, new IntValueToken(0));
  }

  private static void setSimpleExpression(TokenListEditor<Expr> editor) {
    PlusExpr plus = new PlusExpr();
    plus.left.set(new NumberExpr());
    plus.right.set(new NumberExpr());
    editor.value.set(plus);
  }

  private static TokenListEditor<Expr> newTokenListEditor(boolean updateModel) {
    return new TokenListEditor<>(new ExprHybridEditorSpec(), new ObservableArrayList<Token>(), updateModel);
  }

  private TokenListEditor<Expr> editor;

  @Test
  public void autoReprint() {
    editor = newTokenListEditor(true);
    setSimpleExpression(editor);

    assertEquals(simpleTokenList(), editor.tokens);
    assertTrue(editor.valid.get());
  }

  @Test
  public void disabledAutoReprint() {
    editor = newTokenListEditor(false);
    setSimpleExpression(editor);

    assertTrue(editor.tokens.isEmpty());
    assertTrue(editor.valid.get());

    editor.reprintToTokens();
    assertEquals(simpleTokenList(), editor.tokens);
    assertTrue(editor.valid.get());
  }

  @Test
  public void autoReparse() {
    editor = newTokenListEditor(true);
    editor.tokens.add(new IntValueToken(2));

    assertNotNull(editor.value.get());
    assertTrue(editor.valid.get());
  }

  @Test
  public void disabledAutoReparse() {
    editor = newTokenListEditor(false);
    editor.tokens.add(new IntValueToken(2));

    assertNull(editor.value.get());
    assertTrue(editor.valid.get());
  }

  @Test
  public void errorAutoReparsing() {
    editor = newTokenListEditor(true);
    assertNull(editor.value.get());
    assertTrue(editor.valid.get());

    editor.tokens.add(Tokens.PLUS);

    assertNull(editor.value.get());
    assertFalse(editor.valid.get());
  }

  @Test
  public void updateToPrintedDisable() {
    editor = newTokenListEditor(false);
    setSimpleExpression(editor);

    editor.reprintToTokens();
    assertEquals(simpleTokenList(), editor.tokens);

    editor.tokens.set(0, new IntValueToken(1));
    editor.updateToPrintedTokens();
    assertEquals(1, ((IntValueToken)editor.tokens.get(0)).getValue());
  }

  @Test
  public void reparseWithComment() {
    editor = newTokenListEditor(true);
    editor.tokens.add(new IntValueToken(2));
    editor.tokens.add(new TerminatorToken());

    assertNotNull(editor.value.get());
    assertTrue(editor.valid.get());
  }

  @Test
  public void validWhenCommentAtTheBeginning() {
    editor = newTokenListEditor(true);
    editor.tokens.add(new TerminatorToken());

    assertNull(editor.value.get());
    assertTrue(editor.valid.get());
  }
}
