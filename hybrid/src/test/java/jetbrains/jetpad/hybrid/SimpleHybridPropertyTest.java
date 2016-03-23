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

import jetbrains.jetpad.hybrid.parser.ErrorToken;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SimpleHybridPropertyTest extends BaseTestCase {

  private ObservableList<Token> myTokens;
  private HybridProperty<Expr> myProp;

  @Before
  public void init() {
    myTokens = new ObservableArrayList<>();
    HybridEditorSpec<Expr> hybridEditorSpec = new ExprHybridEditorSpec();
    myProp = new SimpleHybridProperty<>(hybridEditorSpec.getParser(), myTokens, hybridEditorSpec.getParsingContextFactory());
  }

  @Test
  public void empty() {
    assertNull(myProp.get());
  }

  @Test
  public void tokenError() {
    myTokens.add(new ErrorToken("something wrong"));
    assertNull(myProp.get());
  }

  @Test
  public void parserError() {
    myTokens.add(Tokens.PLUS);
    assertNull(myProp.get());
  }

  @Test
  public void invalidate() {
    myTokens.add(new IdentifierToken("a"));
    myTokens.add(Tokens.PLUS);
    myTokens.add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);

    myTokens.remove(2);
    assertNull(myProp.get());
  }

  @Test
  public void validate() {
    myTokens.add(new IdentifierToken("a"));
    myTokens.add(Tokens.PLUS);
    assertNull(myProp.get());

    myTokens.add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);
  }
}
