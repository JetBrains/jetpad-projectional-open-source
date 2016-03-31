package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.hybrid.parser.*;
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

import static org.junit.Assert.*;

public class ParsingHybridPropertyTest extends BaseTestCase {

  private ObservableList<Token> mySourceTokens;
  private HybridProperty<Expr> myProp;
  private TestParser parser;

  @Before
  public void init() {
    mySourceTokens = new ObservableArrayList<>();
    final HybridEditorSpec<Expr> hybridEditorSpec = new ExprHybridEditorSpec();
    parser = new TestParser(hybridEditorSpec);
    myProp = new ParsingHybridProperty<>(
      parser, hybridEditorSpec.getPrettyPrinter(), mySourceTokens, HybridEditorSpecUtil.getParsingContextFactory(hybridEditorSpec));
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
