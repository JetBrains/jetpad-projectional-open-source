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
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.NumberExpr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TokenListEditorTest {
  private TokenListEditor<Expr> editor =
    new TokenListEditor<>(new ExprHybridEditorSpec(), new ObservableArrayList<Token>());

  @Test
  public void updating() {
    PlusExpr plus = new PlusExpr();
    plus.left.set(new NumberExpr());
    plus.right.set(new NumberExpr());
    editor.value.set(plus);

    assertEquals(Arrays.asList(new IntValueToken(0), Tokens.PLUS, new IntValueToken(0)), editor.tokens);
    assertTrue(editor.valid.get());
  }

  @Test
  public void errorParsing() {
    editor.tokens.add(Tokens.PLUS);

    assertNull(editor.value.get());
    assertFalse(editor.valid.get());
  }

  @Test
  public void correctParsing() {
    editor.tokens.add(new IntValueToken(2));

    assertNotNull(editor.value.get());
    assertTrue(editor.valid.get());
  }
}