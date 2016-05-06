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

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PrettyHybridPropertyTest extends BaseTestCase {
  private ObservableList<TestTokenWrapper> source;
  private HybridProperty<Expr> prop;
  private TestParser parser;
  private TestRemoveHandler removeHandler;
  private HybridEditorSpec<Expr> editorSpec;

  public void init(Token... tokens) {
    source = new ObservableArrayList<>();
    setSource(tokens);
    editorSpec = new ExprHybridEditorSpec();
    parser = new TestParser(editorSpec);

    PrettyHybridProperty.IndexedTransform<TestTokenWrapper, Token> to = new PrettyHybridProperty.IndexedTransform<TestTokenWrapper, Token>() {
      @Override
      public Token apply(int index, TestTokenWrapper source) {
        return source.token;
      }
    };
    PrettyHybridProperty.IndexedTransform<Token, TestTokenWrapper> from = new PrettyHybridProperty.IndexedTransform<Token, TestTokenWrapper>() {
      @Override
      public TestTokenWrapper apply(int index, Token source) {
        return new TestTokenWrapper(source);
      }
    };

    removeHandler = new TestRemoveHandler();
    prop = new PrettyHybridProperty<>(
      source, to, from,
      removeHandler,
      parser,
      editorSpec.getPrettyPrinter(),
      HybridEditorSpecUtil.getParsingContextFactory(editorSpec));
  }

  @Test
  public void empty() {
    init();
    assertNull(prop.get());
    assertTrue(prop.getTokens().isEmpty());
    assertSync();
  }

  @Test
  public void start() {
    init(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();
  }

  @Test
  public void sourceError() {
    init();
    setSource(Tokens.PLUS);
    assertNull(prop.get());
    assertSync();
  }

  @Test
  public void tokensError() {
    init();
    setTokens(Tokens.PLUS);
    assertNull(prop.get());
    assertSync();
  }

  @Test
  public void tokenError() {
    init();
    setSource(new ErrorToken("something wrong"));
    assertNull(prop.get());
    assertSync();

    setTokens(new ErrorToken("something other wrong"));
    assertNull(prop.get());
    assertSync();
  }

  @Test
  public void validateSource() {
    init();
    setSource(new IdentifierToken("a"), Tokens.PLUS);
    assertNull(prop.get());
    assertSync();

    addSource(new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();
  }

  @Test
  public void invalidateSource() {
    init();
    setSource(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();

    source.remove(2);
    assertNull(prop.get());
    assertSync();
    assertEquals(Arrays.asList(2), removeHandler.invocations);
  }

  @Test
  public void invalidateTokens() {
    init();
    setTokens(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();

    prop.getTokens().remove(2);
    assertNull(prop.get());
    assertSync();
    assertEquals(Arrays.asList(2), removeHandler.invocations);
  }

  @Test
  public void validateTokens() {
    init();
    setTokens(new IdentifierToken("a"), Tokens.PLUS);
    assertNull(prop.get());
    assertSync();

    addTokens(new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();
  }

  @Test
  public void insertion() {
    init();
    prop.getTokens().add(Tokens.PLUS);
    prop.getTokens().add(0, new IdentifierToken("a"));
    prop.getTokens().add(new IdentifierToken("b"));
    assertTrue(prop.get() instanceof PlusExpr);
    assertSync();
  }

  @Test
  public void deletion() {
    init();
    prop.getTokens().add(Tokens.PLUS);
    prop.getTokens().add(new IdentifierToken("b"));
    assertSync();

    prop.getTokens().remove(0);
    assertTrue(prop.get() instanceof VarExpr);
    assertSync();
  }

  @Test
  public void parserInvocations() {
    init();
    final Value<Integer> changes = new Value<>(0);
    Registration r = prop.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) {
        changes.set(changes.get() + 1);
      }
    });

    setSource(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));
    assertEquals(4 /* Initial refresh + 3 changes */,
      parser.invocations);
    assertEquals(Integer.valueOf(3), changes.get());

    source.set(2, new TestTokenWrapper(Tokens.MUL));
    assertEquals(5, parser.invocations);
    assertEquals(Integer.valueOf(4), changes.get());

    r.remove();
  }

  @Test
  public void reprint() {
    init();
    setTokens(Tokens.ID, Tokens.MUL, new IdentifierToken("x"), Tokens.LP, Tokens.RP);

    assertTrue(prop.get() instanceof MulExpr);
    assertSync();
    assertSame(Tokens.LP_CALL, prop.getTokens().get(3));
    assertSame(Tokens.LP_CALL, source.get(3).token);
  }

  @Test
  public void olderPretty() {
    init();
    setTokens(new IdentifierToken("x"), Tokens.LP, Tokens.RP);
    assertSync();
    assertSame(Tokens.LP_CALL, prop.getTokens().get(1));
    assertSame(Tokens.LP_CALL, source.get(1).token);

    addTokens(Tokens.MUL);
    assertSync();
    assertSame(Tokens.LP_CALL, prop.getTokens().get(1));
    assertSame(Tokens.LP_CALL, source.get(1).token);
  }

  @Test
  public void reprintWithListener() {
    init();
    final Value<Integer> sourceChanges = new Value<>(0);
    Registration vr = prop.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) { }
    });
    Registration sr = source.addListener(new CollectionListener<TestTokenWrapper>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends TestTokenWrapper> event) {
        sourceChanges.set(sourceChanges.get() + 1);
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends TestTokenWrapper> event) {
        sourceChanges.set(sourceChanges.get() + 1);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends TestTokenWrapper> event) {
        sourceChanges.set(sourceChanges.get() + 1);
      }
    });

    addSource(new IdentifierToken("x"));
    addTokens(Tokens.LP);
    assertEquals(Integer.valueOf(2), sourceChanges.get());

    assertNull(prop.get());
    assertSync();

    addTokens(Tokens.RP);
    assertEquals(Integer.valueOf(4) /* previous changes + reprint of LP + add of RP */,
      sourceChanges.get());
    assertTrue(prop.get() instanceof CallExpr);
    assertSync();
    assertSame(Tokens.LP_CALL, prop.getTokens().get(1));
    assertSame(Tokens.LP_CALL, source.get(1).token);

    vr.remove();
    sr.remove();
  }

  @Test
  public void valueChangesAfterSync() {
    init(new IdentifierToken("a"), Tokens.PLUS);
    assertNull(prop.get());

    Registration r = prop.addHandler(new EventHandler<PropertyChangeEvent<Expr>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Expr> event) {
        Expr newValue = event.getNewValue();
        if (newValue != null) {
          PrettyPrinterContext<? super Expr> printCtx = new PrettyPrinterContext<>(editorSpec.getPrettyPrinter());
          printCtx.print(newValue);
          assertEquals(printCtx.tokens(), prop.getTokens());
        }
      }
    });

    addSource(new IdentifierToken("b"));

    r.remove();
  }

  @Test
  public void missingCommentOnStart() {
    init(new IdentifierToken("a"), new CommentToken("#", "comment"));
    assertSync();
  }

  @Test
  public void typos() {
    init();
    addTokens(Tokens.ID);
    addTokens(new ErrorToken(" "));
    prop.getTokens().remove(1);
    addTokens(new IdentifierToken("x"));
    addTokens(new ErrorToken(" "));
    prop.getTokens().remove(2);
    addTokens(Tokens.LP);
    addTokens(new ErrorToken(" "));
    prop.getTokens().remove(3);
    addTokens(Tokens.RP);
    assertEquals(Arrays.asList(1, 2, 3), removeHandler.invocations);
    assertSync();
  }

  private void addSource(Token t) {
    source.add(new TestTokenWrapper(t));
  }

  private void addTokens(Token t) {
    prop.getTokens().add(t);
  }

  private void setSource(Token... tokens) {
    int i;
    for (i = 0; i < tokens.length && i < source.size(); i++) {
      source.set(i, new TestTokenWrapper(tokens[i]));
    }
    if (i < tokens.length - 1) {
      for (; i < tokens.length; i++) {
        source.add(new TestTokenWrapper(tokens[i]));
      }
    }
    if (source.size() - 1 > i) {
      source.subList(i + 1, source.size()).clear();
    }
  }

  private void setTokens(Token... tokens) {
    int i;
    for (i = 0; i < tokens.length && i < source.size(); i++) {
      prop.getTokens().set(i, tokens[i]);
    }
    if (i < tokens.length - 1) {
      for (; i < tokens.length; i++) {
        prop.getTokens().add(tokens[i]);
      }
    }
    if (source.size() - 1 > i) {
      prop.getTokens().subList(i + 1, source.size()).clear();
    }
  }

  private void assertSync() {
    assertEquals("Source and tokens size mismatch. Source"  + source + ", tokens: " + prop.getTokens(),
      source.size(), prop.getTokens().size());
    for (int i = 0; i < source.size(); i++) {
      assertEquals(source.get(i).token, prop.getTokens().get(i));
    }
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

  private static class TestRemoveHandler implements Handler<Integer> {
    private List<Integer> invocations = new ArrayList<>();

    @Override
    public void handle(Integer item) {
      invocations.add(item);
    }
  }
}
