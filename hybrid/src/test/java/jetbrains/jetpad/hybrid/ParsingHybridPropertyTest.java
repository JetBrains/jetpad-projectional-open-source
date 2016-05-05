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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.CallExpr;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.MulExpr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ParsingHybridPropertyTest extends BaseTestCase {

  private ObservableList<Token> mySourceTokens;
  private HybridProperty<Expr> myProp;
  private TestParser parser;
  private HybridEditorSpec<Expr> myEditorSpec;

  @Before
  public void init() {
    init(new Token[0]);
  }

  private void init(Token... tokens) {
    mySourceTokens = new ObservableArrayList<>();
    mySourceTokens.addAll(Arrays.asList(tokens));
    myEditorSpec = new ExprHybridEditorSpec();
    parser = new TestParser(myEditorSpec);
    myProp = new ParsingHybridProperty<>(
      parser, myEditorSpec.getPrettyPrinter(), mySourceTokens, HybridEditorSpecUtil.getParsingContextFactory(myEditorSpec));
  }

  @Test
  public void empty() {
    assertNull(myProp.get());
    assertTrue(myProp.getTokens().isEmpty());
  }

  @Test
  public void tokenError() {
    mySourceTokens.add(new ErrorToken("something wrong"));
    assertEquals(mySourceTokens, myProp.getTokens());
    assertNull(myProp.get());

    mySourceTokens.clear();
    myProp.getTokens().add(new ErrorToken("something other wrong"));
    assertEquals(mySourceTokens, myProp.getTokens());
    assertNull(myProp.get());
  }

  @Test
  public void parserError() {
    mySourceTokens.add(Tokens.PLUS);
    assertNull(myProp.get());
  }

  @Test
  public void invalidateSource() {
    mySourceTokens.add(new IdentifierToken("a"));
    mySourceTokens.add(Tokens.PLUS);
    mySourceTokens.add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);
    assertEquals(mySourceTokens, myProp.getTokens());

    mySourceTokens.remove(2);
    assertNull(myProp.get());
    assertEquals(mySourceTokens, myProp.getTokens());
  }

  @Test
  public void validateSource() {
    mySourceTokens.add(new IdentifierToken("a"));
    mySourceTokens.add(Tokens.PLUS);
    assertNull(myProp.get());
    assertEquals(mySourceTokens, myProp.getTokens());

    mySourceTokens.add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);
    assertEquals(mySourceTokens, myProp.getTokens());
  }

  @Test
  public void invalidateTokens() {
    myProp.getTokens().add(new IdentifierToken("a"));
    myProp.getTokens().add(Tokens.PLUS);
    myProp.getTokens().add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);
    assertEquals(mySourceTokens, myProp.getTokens());

    myProp.getTokens().remove(2);
    assertNull(myProp.get());
    assertEquals(mySourceTokens, myProp.getTokens());
  }

  @Test
  public void validateTokens() {
    myProp.getTokens().add(new IdentifierToken("a"));
    myProp.getTokens().add(Tokens.PLUS);
    assertNull(myProp.get());
    assertEquals(mySourceTokens, myProp.getTokens());

    myProp.getTokens().add(new IdentifierToken("b"));
    assertTrue(myProp.get() instanceof PlusExpr);
    assertEquals(mySourceTokens, myProp.getTokens());
  }

  @Test
  public void parserInvocations() {
    Registration r = myProp.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) { }
    });

    mySourceTokens.add(new IdentifierToken("a"));
    mySourceTokens.add(Tokens.PLUS);
    mySourceTokens.add(new IdentifierToken("b"));
    assertEquals(5 /* Initial refresh + on add listener + 3 changes */,
      parser.invocations);

    mySourceTokens.set(2, Tokens.MUL);
    assertEquals(6, parser.invocations);

    r.remove();
  }

  @Test
  public void reprint() {
    mySourceTokens.add(Tokens.ID);
    mySourceTokens.add(Tokens.MUL);
    mySourceTokens.add(new IdentifierToken("x"));
    mySourceTokens.add(Tokens.LP);
    mySourceTokens.add(Tokens.RP);

    assertTrue(myProp.get() instanceof MulExpr);
    assertSame(myProp.getTokens().get(3), Tokens.LP_CALL);
  }

  @Test
  public void previousFormatting() {
    mySourceTokens.add(new IdentifierToken("x"));
    mySourceTokens.add(Tokens.LP);
    mySourceTokens.add(Tokens.RP);
    assertSame(myProp.getTokens().get(1), Tokens.LP_CALL);

    mySourceTokens.add(Tokens.MUL);
    assertSame(myProp.getTokens().get(1), Tokens.LP_CALL);
  }

  @Test
  public void withListener() {
    Registration r = myProp.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) { }
    });

    mySourceTokens.add(new IdentifierToken("x"));
    myProp.getTokens().add(Tokens.LP);

    assertNull(myProp.get());
    assertEquals(mySourceTokens, myProp.getTokens());

    myProp.getTokens().add(Tokens.RP);
    assertTrue(myProp.get() instanceof CallExpr);
    assertSame(myProp.getTokens().get(1), Tokens.LP_CALL);

    r.remove();
  }

  @Test
  public void missingCommentOnStart() {
    init(new IdentifierToken("a"), new CommentToken("#", "comment"));
    assertEquals(mySourceTokens, myProp.getTokens());
  }

  @Test
  public void valueChangesAfterSync() {
    init(new IdentifierToken("a"), Tokens.PLUS);
    assertNull(myProp.get());

    Registration r = myProp.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) {
        Expr newValue = event.getNewValue();
        if (newValue != null) {
          PrettyPrinterContext<? super Expr> printCtx = new PrettyPrinterContext<>(myEditorSpec.getPrettyPrinter());
          printCtx.print(newValue);
          assertEquals(printCtx.tokens(), myProp.getTokens());
        }
      }
    });

    mySourceTokens.add(new IdentifierToken("b"));

    r.remove();
  }

  private static class TestParser implements Parser<Expr> {
    private final HybridEditorSpec<Expr> myHybridEditorSpec;
    private int invocations = 0;

    public TestParser(HybridEditorSpec<Expr> hybridEditorSpec) {
      myHybridEditorSpec = hybridEditorSpec;
    }

    @Override
    public Expr parse(ParsingContext ctx) {
      invocations++;
      return myHybridEditorSpec.getParser().parse(ctx);
    }
  }
}
